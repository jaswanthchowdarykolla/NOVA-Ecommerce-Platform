import { apiRequest } from "./httpClient";

export type CreateOrderRequest = {
  items: { productId: number; quantity: number; size?: string }[];
  recipientName: string;
  recipientPhone: string;
  deliveryAddress: string;
  contactEmail?: string;
};

export type OrderSummary = {
  id: number;
  status: string;
  totalAmount: number;
  createdAt: string;
  recipientName?: string;
  recipientPhone?: string;
  deliveryAddress?: string;
  contactEmail?: string;
  itemsJson: string;
};

export const createOrder = (body: CreateOrderRequest) =>
  apiRequest<OrderSummary>("/orders", { method: "POST", body: JSON.stringify(body) });

export const getMyOrders = () => apiRequest<OrderSummary[]>("/orders/mine");

export const getOrderById = (id: number) => apiRequest<OrderSummary>(`/orders/${id}`);
