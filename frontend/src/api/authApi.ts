import { apiRequest } from "./httpClient";
import type { AuthResponse, SigninRequest, SignupRequest } from "../types/auth";

export type UserProfile = {
  id: number;
  name: string;
  email: string;
  role: string;
};

export const signup = (body: SignupRequest) =>
  apiRequest<{ message: string }>("/auth/signup", { method: "POST", body: JSON.stringify(body) });

export const signin = (body: SigninRequest) =>
  apiRequest<AuthResponse>("/auth/signin", { method: "POST", body: JSON.stringify(body) });

export const getProfile = () => apiRequest<UserProfile>("/auth/me");

export const updateProfile = (body: { name: string; newPassword?: string }) =>
  apiRequest<UserProfile>("/auth/profile", { method: "PUT", body: JSON.stringify(body) });
