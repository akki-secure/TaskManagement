import { Draggable } from '@hello-pangea/dnd'

const PRIORITY_LABEL = { high: '高', medium: '中', low: '低' }
const STATUS_LABEL = { todo: '未完了', in_progress: '進行中', done: '完了' }
const STATUS_CLASS = { todo: 'status-todo', in_progress: 'status-in-progress', done: 'status-done' }

function isOverdue(dateStr) {
  if (!dateStr) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return new Date(dateStr) < today
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const [, m, d] = dateStr.split('-')
  return `${parseInt(m)}/${parseInt(d)}`
}

export default function Card({ card, index, onEdit, onDelete }) {
  return (
    <Draggable draggableId={String(card.id)} index={index}>
      {(provided, snapshot) => (
        <div
          ref={provided.innerRef}
          {...provided.draggableProps}
          {...provided.dragHandleProps}
        >
          <div
            className={`card ${snapshot.isDragging ? 'sortable-chosen' : ''}`}
            onClick={(e) => { if (!e.target.closest('button')) onEdit(card) }}
          >
            <div className="card-header">
              <span className="card-title">{card.title || '無題のカード'}</span>
              <button
                className="btn-delete-card"
                onClick={e => { e.stopPropagation(); onDelete(card.id) }}
                title="カードを削除"
              >×</button>
            </div>
            <div className="card-meta">
              <span className={`status-badge ${STATUS_CLASS[card.status] || STATUS_CLASS.todo}`}>
                {STATUS_LABEL[card.status] || '未完了'}
              </span>
              {card.dueDate && (
                <span className={`card-due ${isOverdue(card.dueDate) ? 'overdue' : ''}`}>
                  📅 {formatDate(card.dueDate)}
                </span>
              )}
              {card.priority && (
                <span className={`priority-badge ${card.priority}`}>
                  {PRIORITY_LABEL[card.priority]}
                </span>
              )}
            </div>
          </div>
        </div>
      )}
    </Draggable>
  )
}
