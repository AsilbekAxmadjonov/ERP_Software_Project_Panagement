const paths = {
  login: '/login',
  home: '/',
  'users.index': '/users',
  'partners.index': '/partners',
} as const;

export type RouteName = keyof typeof paths;

export const route = (name: RouteName): string => paths[name];
