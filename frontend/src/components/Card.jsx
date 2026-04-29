import { Draggable } from '@hello-pangea/dnd'

const PRIORITY_LABEL = { high: '高', medium: '中', low: '低' }

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
          className={`card ${snapshot.isDragging ? 'sortable-chosen' : ''}`}
          ref={provided.innerRef}
          {...provided.draggableProps}
          {...provided.dragHandleProps}
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
          {(card.dueDate || card.priority) && (
            <div className="card-meta">
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
          )}
        </div>
      )}
    </Draggable>
  )
}
