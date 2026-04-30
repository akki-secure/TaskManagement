import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
})

api.interceptors.response.use(
    res => res,
    err => {
        if (err.response?.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            window.location.href = '/login'
        }
        return Promise.reject(err)
    }
)

export const register = (data) => api.post('/auth/register', data).then(r => r.data)
export const loginApi = (data) => api.post('/auth/login', data).then(r => r.data)

export const getMyBoards = () => api.get('/boards').then(r => r.data)
export const getBoard = (id) => api.get(`/boards/${id}`).then(r => r.data)

export const createList = (boardId, title) =>
    api.post('/lists', { boardId, title }).then(r => r.data)

export const updateList = (id, title) =>
    api.put(`/lists/${id}`, { title }).then(r => r.data)

export const deleteList = (id) => api.delete(`/lists/${id}`)

export const reorderLists = (items) => api.put('/lists/reorder', items)

export const createCard = (listId) =>
    api.post('/cards', { listId }).then(r => r.data)

export const updateCard = (id, data) =>
    api.put(`/cards/${id}`, data).then(r => r.data)

export const deleteCard = (id) => api.delete(`/cards/${id}`)

export const moveCard = (id, toListId, position) =>
    api.put(`/cards/${id}/move`, { toListId, position }).then(r => r.data)
