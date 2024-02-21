import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
    stages: [
        { duration: '10m', target: 100 }, // Adjust target VUs based on your system's capability
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'], // 95% of requests should complete below 2 seconds
    },
};

function generatePayload() {
    // Generating a unique OrderId for each request to simulate different orders
    
    return JSON.stringify({
        OrderId: 12345,
        Symbol: "BTC",
        Quantity: 10, // Quantity between 1 and 100
    });
}

export default function () {
    const url = 'http://localhost:3000/placeOrder';
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    const payload = generatePayload();

    http.post(url, payload, params);

    // Sleep time calculation to spread 50,000 requests evenly over 600 seconds (10 minutes)
    // This should be adjusted based on the number of VUs and actual request duration observed during initial tests
    sleep(0.01);
}
