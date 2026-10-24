import { apiRequest } from "./httpClient";

export type Address = {
  id: number;
  recipientName: string;
  phone: string;
  addressLine1: string;
  addressLine2?: string;
  locality?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  isDefault: boolean;
  createdAt?: string;
  updatedAt?: string;
};

export type AddressInput = {
  recipientName: string;
  phone: string;
  addressLine1: string;
  addressLine2?: string;
  locality?: string;
  city: string;
  state: string;
  postalCode: string;
  country?: string;
  isDefault?: boolean;
};

export const listAddresses = () => apiRequest<Address[]>("/addresses");
export const createAddress = (body: AddressInput) => apiRequest<Address>("/addresses", { method: "POST", body: JSON.stringify(body) });
export const updateAddress = (id: number, body: AddressInput) => apiRequest<Address>(`/addresses/${id}`, { method: "PUT", body: JSON.stringify(body) });
export const deleteAddress = (id: number) => apiRequest<void>(`/addresses/${id}`, { method: "DELETE" });
export const setDefaultAddress = (id: number) => apiRequest<Address>(`/addresses/${id}/default`, { method: "PUT" });
