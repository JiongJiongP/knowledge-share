import request from '@/utils/request'

export function getSensitiveWords() {
  return request.get('/admin/sensitive-words')
}

export function addSensitiveWord(data) {
  return request.post('/admin/sensitive-words', data)
}

export function deleteSensitiveWord(id) {
  return request.delete(`/admin/sensitive-words/${id}`)
}

export function batchImportSensitiveWords(words, category) {
  return request.post('/admin/sensitive-words/batch', { words, category })
}

export function checkSensitiveWords(text) {
  return request.post('/admin/sensitive-words/check', { text })
}
