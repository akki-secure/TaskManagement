import { useState, useEffect } from 'react'

const STATUS_OPTIONS = [
  { value: 'todo', label: '未完了' },
  { value: 'in_progress', label: '進行中' },
  { value: 'done', label: '完了' },
]

function formatDateTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

export default function CardModal({ card, onSave, onClose, onDelete }) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [dueDate, setDueDate] = useState('')
  const [priority, setPriority] = useState('')
  const [status, setStatus] = useState('todo')

  useEffect(() => {
    if (card) {
      setTitle(card.title === '無題のカード' ? '' : card.title)
      setDescription(card.description || '')
      setDueDate(card.dueDate || '')
      setPriority(card.priority || '')
      setStatus(card.status || 'todo')
    }
  }, [card])

  if (!card) return null

  function handleSave() {
    onSave({ title, description, dueDate, priority, status })
  }

  function handleOverlayClick(e) {
    if (e.target === e.currentTarget) onClose()
  }

  const priorityOptions = [
    { value: 'high', label: '高' },
    { value: 'medium', label: '中' },
    { value: 'low', label: '低' },
  ]

  return (
    <div className="modal-overlay open" onClick={handleOverlayClick}>
      <div className="modal">
        <header className="modal-header">
          <h2>カードを編集</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </header>
        <div className="modal-body">
          <div>
            <label htmlFor="m-title">タイトル</label>
            <input
              id="m-title"
              type="text"
              className="modal-input"
              value={title}
              placeholder="タスク名を入力"
              onChange={e => setTitle(e.target.value)}
              onKeyDown={e => { if (e.key === 'Enter') handleSave(); if (e.key === 'Escape') onClose() }}
              autoFocus
            />
          </div>
          <div>
            <label htmlFor="m-desc">説明</label>
            <textarea
              id="m-desc"
              className="modal-input modal-textarea"
              value={description}
              placeholder="詳細・メモを入力"
              rows={4}
              onChange={e => setDescription(e.target.value)}
            />
          </div>
          <div>
            <label htmlFor="m-due">期限</label>
            <input
              id="m-due"
              type="date"
              className="modal-input"
              value={dueDate}
              onChange={e => setDueDate(e.target.value)}
            />
          </div>
          <div>
            <label>優先度</label>
            <div className="priority-selector">
              {priorityOptions.map(opt => (
                <button
                  key={opt.value}
                  className={`priority-btn ${priority === opt.value ? `selected-${opt.value}` : ''}`}
                  onClick={() => setPriority(priority === opt.value ? '' : opt.value)}
                >
                  {opt.label}
                </button>
              ))}
            </div>
            {priority && (
              <button className="priority-btn-none" onClick={() => setPriority('')}>
                優先度をクリア
              </button>
            )}
          </div>
          <div>
            <label>ステータス</label>
            <div className="status-selector">
              {STATUS_OPTIONS.map(opt => (
                <button
                  key={opt.value}
                  className={`status-btn ${status === opt.value ? `selected-${opt.value}` : ''}`}
                  onClick={() => setStatus(opt.value)}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          </div>
          {(card.createdAt || card.updatedAt) && (
            <div className="card-timestamps">
              {card.createdAt && <span>作成日: {formatDateTime(card.createdAt)}</span>}
              {card.updatedAt && <span>更新日: {formatDateTime(card.updatedAt)}</span>}
            </div>
          )}
        </div>
        <footer className="modal-footer">
          {onDelete && (
            <button className="modal-delete" onClick={() => onDelete(card.id)}>削除</button>
          )}
          <div style={{ marginLeft: 'auto', display: 'flex', gap: 8 }}>
            <button className="modal-cancel" onClick={onClose}>キャンセル</button>
            <button className="modal-save" onClick={handleSave}>保存</button>
          </div>
        </footer>
      </div>
    </div>
  )
}
