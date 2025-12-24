import axios from "axios";

const BASE_URL = "http://localhost:8080/api/orders";

export const getTax = (token) => {
    return axios.get(`${BASE_URL}/tax`, {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    });
};

export const setTax = (token, rate) => {
    return axios.put(
        `${BASE_URL}/tax`,
        { rate },
        {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        }
    );
};
