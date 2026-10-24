import { apiRequest } from "./httpClient";

export type PaymentOrder = {
  keyId: string;
  razorpayOrderId: string;
  amount: number;
  currency: string;
  localOrderId: number;
};

export type PaymentSummary = {
  id: number;
  orderId: number;
  razorpayOrderId: string;
  razorpayPaymentId?: string;
  status: string;
  amount: number;
  updatedAt: string;
  orderCreatedAt: string;
};

export const createPaymentOrder = (orderId: number) =>
  apiRequest<PaymentOrder>(`/payments/create-order?orderId=${orderId}`, { method: "POST" });

export const verifyPayment = (body: {
  orderId: number;
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
}) =>
  apiRequest<{ status: string; orderId: number; paymentId: string }>("/payments/verify", {
    method: "POST",
    body: JSON.stringify(body),
  });

export const getMyPayments = () => apiRequest<PaymentSummary[]>("/payments/mine");
