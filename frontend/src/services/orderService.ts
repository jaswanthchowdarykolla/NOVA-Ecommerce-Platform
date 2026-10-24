import { createOrder, getMyOrders } from "../api/orderApi";
import type { CreateOrderRequest } from "../api/orderApi";
export const orderService={create:(input:CreateOrderRequest)=>createOrder(input),listMine:getMyOrders};
