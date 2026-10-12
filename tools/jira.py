#!/usr/bin/env python
"""Minimal Jira Cloud CLI (stdlib only).

Env:
  JIRA_URL        site base URL, e.g. https://yourcompany.atlassian.net (or pass --url)
  JIRA_ID         account email used for basic auth
  JIRA_API_TOKEN  API token

Usage:
  python tools/jira.py projects
  python tools/jira.py mine [--project KEY]
  python tools/jira.py search "project = ERP AND status = 'In Progress'"
  python tools/jira.py issue ERP-12
  python tools/jira.py members ERP
  python tools/jira.py distribute ERP [--exclude "Name"] [--apply]   # dry run unless --apply
"""
import argparse
import base64
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request

FIELDS = ["summary", "status", "assignee", "priority", "issuetype"]


def request(base, method, path, body=None):
    email, token = os.environ.get("JIRA_ID"), os.environ.get("JIRA_API_TOKEN")
    if not email or not token:
        sys.exit("JIRA_ID and JIRA_API_TOKEN must be set")
    auth = base64.b64encode(f"{email}:{token}".encode()).decode()
    req = urllib.request.Request(
        base.rstrip("/") + path,
        data=json.dumps(body).encode() if body is not None else None,
        method=method,
        headers={
            "Authorization": f"Basic {auth}",
            "Accept": "application/json",
            "Content-Type": "application/json",
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            raw = resp.read()
            return json.loads(raw) if raw else {}
    except urllib.error.HTTPError as e:
        sys.exit(f"HTTP {e.code} {e.reason}: {e.read().decode(errors='replace')[:500]}")
    except urllib.error.URLError as e:
        sys.exit(f"Connection failed: {e.reason}")


def name(obj, key="name"):
    return (obj or {}).get(key) or "-"


def print_issues(issues):
    for i in issues:
        f = i["fields"]
        print(
            f"{i['key']:<10} {name(f.get('status')):<14} {name(f.get('priority')):<8} "
            f"{name(f.get('assignee'), 'displayName'):<20} {f.get('summary', '')}"
        )
    print(f"\n{len(issues)} issue(s)")


def search(base, jql, limit=50):
    data = request(base, "POST", "/rest/api/3/search/jql",
                   {"jql": jql, "fields": FIELDS, "maxResults": limit})
    print_issues(data.get("issues", []))


def members(base, project):
    users = request(base, "GET", "/rest/api/3/user/assignable/search?"
                    + urllib.parse.urlencode({"project": project, "maxResults": 100}))
    return [u for u in users if u.get("accountType") == "atlassian" and u.get("active", True)]


def distribute(base, project, exclude, apply):
    team = [u for u in members(base, project)
            if not any(x.lower() in u["displayName"].lower() for x in exclude)]
    if not team:
        sys.exit("No assignable members found")
    load = {u["accountId"]: 0 for u in team}
    open_jql = f"project = {project} AND statusCategory != Done"
    for i in request(base, "POST", "/rest/api/3/search/jql",
                     {"jql": open_jql + " AND assignee is not EMPTY",
                      "fields": ["assignee"], "maxResults": 1000}).get("issues", []):
        acc = i["fields"]["assignee"]["accountId"]
        if acc in load:
            load[acc] += 1
    todo = request(base, "POST", "/rest/api/3/search/jql",
                   {"jql": open_jql + " AND assignee is EMPTY ORDER BY priority DESC, created ASC",
                    "fields": FIELDS, "maxResults": 200}).get("issues", [])
    by_id = {u["accountId"]: u for u in team}
    print("Current open load:", ", ".join(f"{by_id[a]['displayName']}={n}" for a, n in load.items()))
    for i in todo:
        acc = min(load, key=load.get)  # least-loaded member gets the next issue
        load[acc] += 1
        print(f"{i['key']:<10} -> {by_id[acc]['displayName']:<22} {i['fields'].get('summary', '')}")
        if apply:
            request(base, "PUT", f"/rest/api/3/issue/{i['key']}/assignee", {"accountId": acc})
    print(f"\n{len(todo)} issue(s) {'assigned' if apply else 'planned (dry run, use --apply)'}")


def adf_text(node):
    """Flatten Atlassian Document Format to plain text."""
    if not node:
        return ""
    if isinstance(node, str):
        return node
    if node.get("type") == "text":
        return node.get("text", "")
    sep = "\n" if node.get("type") in ("paragraph", "heading", "listItem") else ""
    return "".join(adf_text(c) for c in node.get("content", [])) + sep


def main():
    p = argparse.ArgumentParser(description="Jira CLI")
    p.add_argument("--url", default=os.environ.get("JIRA_URL"))
    sub = p.add_subparsers(dest="cmd", required=True)
    sub.add_parser("projects")
    m = sub.add_parser("mine")
    m.add_argument("--project")
    s = sub.add_parser("search")
    s.add_argument("jql")
    s.add_argument("--limit", type=int, default=50)
    sub.add_parser("members").add_argument("project")
    d = sub.add_parser("distribute")
    d.add_argument("project")
    d.add_argument("--exclude", action="append", default=[])
    d.add_argument("--apply", action="store_true")
    i = sub.add_parser("issue")
    i.add_argument("key")
    a = p.parse_args()

    if not a.url:
        sys.exit("Set JIRA_URL or pass --url https://yourcompany.atlassian.net")

    if a.cmd == "projects":
        for pr in request(a.url, "GET", "/rest/api/3/project/search").get("values", []):
            print(f"{pr['key']:<10} {pr['name']}")
    elif a.cmd == "mine":
        jql = "assignee = currentUser() AND statusCategory != Done"
        if a.project:
            jql += f" AND project = {a.project}"
        search(a.url, jql + " ORDER BY updated DESC")
    elif a.cmd == "search":
        search(a.url, a.jql, a.limit)
    elif a.cmd == "members":
        for u in members(a.url, a.project):
            print(f"{u['displayName']:<25} {u['accountId']}")
    elif a.cmd == "distribute":
        distribute(a.url, a.project, a.exclude, a.apply)
    elif a.cmd == "issue":
        d = request(a.url, "GET", f"/rest/api/3/issue/{urllib.parse.quote(a.key)}")
        f = d["fields"]
        print(f"{d['key']}: {f.get('summary')}")
        print(f"Type: {name(f.get('issuetype'))} | Status: {name(f.get('status'))} | "
              f"Priority: {name(f.get('priority'))}")
        print(f"Assignee: {name(f.get('assignee'), 'displayName')} | "
              f"Reporter: {name(f.get('reporter'), 'displayName')}")
        print("\n" + (adf_text(f.get("description")).strip() or "(no description)"))


if __name__ == "__main__":
    main()
