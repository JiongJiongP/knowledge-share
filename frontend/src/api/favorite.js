import request from '@/utils/request'

export function getFavorites(params) {
  return request.get('/favorites', { params })
}

export function favoriteContent(contentId) {
  return request.post(`/favorites/${contentId}`)
}

export function unfavoriteContent(contentId) {
  return request.delete(`/favorites/${contentId}`)
}

export function checkFavorite(contentId) {
  return request.get(`/favorites/check/${contentId}`)
}
