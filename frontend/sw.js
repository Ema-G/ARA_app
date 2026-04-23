const CACHE_NAME = 'ara-app-v3';
const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/app.html',
  '/pages/login.html',
  '/pages/register.html',
  '/pages/forgot-password.html',
  '/pages/thankyou.html',
  '/css/style.css',
  '/js/auth.js',
  '/js/app.js',
  '/manifest.json'
  // reset-password.html is NOT cached — it depends on a URL token
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(STATIC_ASSETS))
  );
  self.skipWaiting();
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

self.addEventListener('fetch', event => {
  const url = new URL(event.request.url);

  // Network-first for API calls (same-origin only — cross-origin requests bypass the SW)
  if (url.origin === self.location.origin && url.pathname.startsWith('/api/')) {
    event.respondWith(
      fetch(event.request).catch(() =>
        new Response(JSON.stringify({ error: 'You are offline. Please reconnect to submit assessments.' }),
          { status: 503, headers: { 'Content-Type': 'application/json' } })
      )
    );
    return;
  }

  // Cache-first for static assets
  event.respondWith(
    caches.match(event.request).then(cached => cached || fetch(event.request))
  );
});

self.addEventListener('push', event => {
  let data = { title: 'ARA', body: 'You have a new notification.', icon: '/icons/icon-192.svg', url: '/app.html' };
  try { if (event.data) data = { ...data, ...event.data.json() }; } catch {}

  event.waitUntil(
    self.registration.showNotification(data.title, {
      body: data.body,
      icon: data.icon,
      badge: '/icons/icon-192.svg',
      data: { url: data.url }
    })
  );
});

self.addEventListener('notificationclick', event => {
  event.notification.close();
  const target = event.notification.data?.url || '/app.html';
  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then(list => {
      const existing = list.find(c => c.url.includes(target));
      return existing ? existing.focus() : clients.openWindow(target);
    })
  );
});
