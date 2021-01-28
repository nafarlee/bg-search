import https from 'https';

export default function get(url) {
  return new Promise((resolve, reject) => {
    https.get(url, (res) => {
      if (res.statusCode < 200 || res.statusCode >= 300) {
        reject(res.statusCode);
        return;
      }
      const body = [];
      res.on('data', (chunk) => body.push(chunk));
      res.on('end', () => resolve(Buffer.concat(body).toString()));
    })
      .on('error', (err) => reject(err));
  });
}
