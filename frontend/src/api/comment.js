import request from '@/utils/request'

export function getComments(contentId) {
  return request.get(`/contents/${contentId}/comments`)
}

export function getReplies(commentId, contentId) {
  return request.get(`/comments/${commentId}/replies`, { params: { contentId } })
}

export function createComment(contentId, data) {
  return request.post(`/contents/${contentId}/comments`, data)
}

export function likeComment(id) {
  return request.post(`/comments/${id}/like`)
}

export function unlikeComment(id) {
  return request.delete(`/comments/${id}/like`)
}

export function deleteComment(id) {
  return request.delete(`/comments/${id}`)
}
