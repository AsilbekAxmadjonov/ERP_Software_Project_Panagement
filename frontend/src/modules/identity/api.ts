import { http } from '../../lib/http';
import type { LoginResult, User, UserRole } from './types';

export interface CreateUserInput {
  username: string;
  password: string;
  fullName: string;
  role: UserRole;
}

export const identityApi = {
  login: (username: string, password: string) => http.post<LoginResult>('/api/auth/login', { username, password }),
  me: () => http.get<User>('/api/auth/me'),
  listUsers: () => http.get<User[]>('/api/users'),
  createUser: (input: CreateUserInput) => http.post<User>('/api/users', input),
  updateUser: (id: number, input: Partial<Pick<User, 'fullName' | 'role' | 'active'>> & { password?: string }) =>
    http.put<User>(`/api/users/${id}`, input),
};
