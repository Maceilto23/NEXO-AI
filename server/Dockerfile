FROM php:8.3-apache

RUN apt-get update \
    && apt-get install -y --no-install-recommends libcurl4-openssl-dev \
    && docker-php-ext-install curl \
    && rm -rf /var/lib/apt/lists/*

COPY . /var/www/html/
RUN chmod +x /var/www/html/docker-entrypoint.sh

ENV PORT=10000
EXPOSE 10000
ENTRYPOINT ["/var/www/html/docker-entrypoint.sh"]
