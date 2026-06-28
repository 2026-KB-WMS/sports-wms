/**
 * fetch 래퍼. 프록시를 통해 Spring으로 요청.
 * 세션 쿠키는 브라우저가 자동으로 포함시킴 (credentials: 'include')
 */
async function request(method, url, body) {
  const options = {
    method,
    credentials: 'include',
    headers: {},
  }
  if (body) {
    options.headers['Content-Type'] = 'application/json'
    options.body = JSON.stringify(body)
  }

  const res = await fetch(url, options)

  if (res.status === 204) return null

  const text = await res.text()
  if (!text) return null

  const contentType = res.headers.get('content-type')
  const data = contentType && contentType.includes('application/json')
    ? JSON.parse(text)
    : null

  if (!res.ok) {
    throw new Error((data && data.message) || `${res.status} ${res.url}`)
  }

  return data
}

export const http = {
  get: (url) => request('GET', url),
  post: (url, body) => request('POST', url, body),
  patch: (url, body) => request('PATCH', url, body),
  delete: (url) => request('DELETE', url),
}
