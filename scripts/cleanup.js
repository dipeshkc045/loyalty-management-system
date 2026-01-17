const http = require('http');

// Helper to make HTTP request
const request = (method, port, path) => {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: 'localhost',
            port: port,
            path: path,
            method: method,
        };

        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', (chunk) => data += chunk);
            res.on('end', () => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    resolve(data ? JSON.parse(data) : null);
                } else {
                    reject(`Request failed with status ${res.statusCode}: ${data}`);
                }
            });
        });

        req.on('error', (e) => reject(e));
        req.end();
    });
};

const run = async () => {
    try {
        console.log('Deleting all transactions...');
        await request('DELETE', 8082, '/api/v1/transactions');
        console.log('All transactions deleted.');

        console.log('Fetching members...');
        const memberPage = await request('GET', 8081, '/api/v1/members?size=100');
        const members = memberPage.content || [];
        console.log(`Found ${members.length} members.`);

        // Keep first 2 members (sorted by ID usually, but here just first 2 in list)
        // If we want to keep specific IDs, we'd filter. But "only 2 users" implies quantity.
        if (members.length > 2) {
            const toDelete = members.slice(2);
            console.log(`Deleting ${toDelete.length} extra members...`);

            for (const member of toDelete) {
                console.log(`Deleting member ${member.id} (${member.name})...`);
                await request('DELETE', 8081, `/api/v1/members/${member.id}`);
            }
        } else {
            console.log('Member count is <= 2. No deletions needed.');
        }

        console.log('Cleanup complete!');
    } catch (err) {
        console.error('Error during cleanup:', err);
    }
};

run();
