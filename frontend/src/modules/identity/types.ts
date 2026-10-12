export const UserRole = { CEO: 'CEO', MANAGER: 'MANAGER', ACCOUNTANT: 'ACCOUNTANT' } as const;
export type UserRole = (typeof UserRole)[keyof typeof UserRole];

export interface User {
  id: number;
  username: string;
  fullName: string | null;
  role: UserRole;
  active: boolean;
}

export interface LoginResult {
  token: string;
  user: User;
}
