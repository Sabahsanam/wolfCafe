import axios from 'axios';

import {getToken} from './AuthService';

const BASE_URL = 'http://localhost:8080/api/orders';

export const getAllOrders = async () => {
    return axios.get(BASE_URL, {
        headers: { Authorization: `Bearer ${getToken()}`}
    });
};

export const updateOrderStatus = async (orderID , newStatus) => {
    return axios.put(
        `${BASE_URL}/${orderID}/status`,
        `"${newStatus}"`,
        {
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${getToken()}`
            }
        }
    );
};

export const getOrdersByUser = async (username) => {
    return axios.get(`${BASE_URL}/user/${username}`, {
        headers: { Authorization: `Bearer ${getToken()}`}
    });
};

export const placeOrder = async (orderDto) => {
    return axios.post(`${BASE_URL}`, orderDto, {
        headers: {Authorization: `Bearer ${getToken()}`}
    });
};