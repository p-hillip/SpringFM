sudo tail -f /var/log/nginx/access.log /var/log/nginx/error.log
tail -F /var/log/sfks/sfks.log
tail -F /var/log/sfks/err.log

sudo nginx -t && sudo systemctl reload nginx
