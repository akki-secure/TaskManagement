import { useState, useRef, useEffect } from 'react'
import { Droppable } from '@hello-pangea/dnd'
import Card from './Card'

export default function List({ list, onAddCard, onDeleteList, onRenameList, onEditCard, onDeleteCard }) {
  const [editing, setEditing] = useState(false)
  const [titleVal, setTitleVal] = useState(list.title)
  const inputRef = useRef(null)

  useEffect(() => { setTitleVal(list.title) }, [list.title])

  useEffect(() => {
    if (editing && inputRef.current) {
      inputRef.current.focus()
      inputRef.current.select()
    }
  }, [editing])

  function commitRename() {
    const val = titleVal.trim()
    if (val && val !== list.title) onRenameList(list.id, val)
    setEditing(false)
  }

  return (
    <div className="list-column">
      <div className="list-header">
        {editing ? (
          <input
            ref={inputRef}
            className="list-title-input"
            value={titleVal}
            onChange={e => setTitleVal(e.target.value)}
            onBlur={commitRename}
            onKeyDown={e => {
              if (e.key === 'Enter') { e.preventDefault(); commitRename() }
              if (e.key === 'Escape') { setTitleVal(list.title); setEditing(false) }
            }}
          />
        ) : (
          <span className="list-title" title="クリックして編集" onClick={() => setEditing(true)}>
            {list.title}
          </span>
        )}
        <button className="btn-delete-list" onClick={() => onDeleteList(list.id)} title="リストを削除">×</button>
      </div>

      <Droppable droppableId={String(list.id)}>
        {(provided, snapshot) => (
          <div
            className="card-list"
            ref={provided.innerRef}
            {...provided.droppableProps}
            style={{ background: snapshot.isDraggingOver ? '#d4e4f7' : undefined }}
          >
            {list.cards.length === 0 && !snapshot.isDraggingOver && (
              <div className="card-list-empty">カードがありません</div>
            )}
            {list.cards.map((card, index) => (
              <Card
                key={card.id}
                card={card}
                index={index}
                onEdit={onEditCard}
                onDelete={onDeleteCard}
              />
            ))}
            {provided.placeholder}
          </div>
        )}
      </Droppable>

      <button className="btn-add-card" onClick={() => onAddCard(list.id)}>＋ カード追加</button>
    </div>
  )
}
