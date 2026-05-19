const HOP_BY_HOP_HEADERS = new Set([
  "connection",
  "keep-alive",
  "proxy-authenticate",
  "proxy-authorization",
  "te",
  "trailer",
  "transfer-encoding",
  "upgrade",
  "host",
  "content-length"
]);

module.exports = async function handler(req, res) {
  const backendUrl = normalizeBackendUrl(process.env.BRMC_BACKEND_URL);
  if (!backendUrl) {
    res.statusCode = 500;
    res.setHeader("content-type", "text/plain; charset=utf-8");
    res.end("Falta configurar BRMC_BACKEND_URL en Vercel.");
    return;
  }

  const targetUrl = buildTargetUrl(backendUrl, req);
  const requestHeaders = copyRequestHeaders(req.headers);

  const response = await fetch(targetUrl, {
    method: req.method,
    headers: requestHeaders,
    body: hasBody(req.method) ? req : undefined,
    redirect: "manual"
  });

  res.statusCode = response.status;
  response.headers.forEach((value, key) => {
    if (!HOP_BY_HOP_HEADERS.has(key.toLowerCase())) {
      res.setHeader(key, rewriteLocation(value, key, backendUrl, req));
    }
  });

  const setCookie = response.headers.getSetCookie ? response.headers.getSetCookie() : [];
  if (setCookie.length > 0) {
    res.setHeader("set-cookie", setCookie.map((cookie) => stripCookieDomain(cookie)));
  }

  const body = Buffer.from(await response.arrayBuffer());
  res.end(body);
};

function normalizeBackendUrl(value) {
  if (!value || !value.trim()) {
    return null;
  }
  return value.trim().replace(/\/+$/, "");
}

function buildTargetUrl(backendUrl, req) {
  const rawPath = Array.isArray(req.query.path) ? req.query.path.join("/") : (req.query.path || "");
  const path = rawPath ? `/${rawPath}` : "/";
  const url = new URL(path, backendUrl);

  for (const [key, value] of Object.entries(req.query)) {
    if (key === "path") {
      continue;
    }
    if (Array.isArray(value)) {
      value.forEach((item) => url.searchParams.append(key, item));
    } else if (value !== undefined) {
      url.searchParams.set(key, value);
    }
  }
  return url;
}

function copyRequestHeaders(headers) {
  const copied = {};
  for (const [key, value] of Object.entries(headers)) {
    if (!HOP_BY_HOP_HEADERS.has(key.toLowerCase()) && value !== undefined) {
      copied[key] = value;
    }
  }
  return copied;
}

function hasBody(method) {
  return !["GET", "HEAD"].includes(method.toUpperCase());
}

function rewriteLocation(value, key, backendUrl, req) {
  if (key.toLowerCase() !== "location") {
    return value;
  }
  if (value.startsWith(backendUrl)) {
    const publicOrigin = `https://${req.headers.host}`;
    return value.replace(backendUrl, publicOrigin);
  }
  return value;
}

function stripCookieDomain(cookie) {
  return cookie.replace(/;\s*domain=[^;]+/i, "");
}
