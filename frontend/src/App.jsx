import { useState, useEffect, useCallback } from 'react'
import { DragDropContext } from '@hello-pangea/dnd'
import List from './components/List'
import CardModal from './components/CardModal'
import {
  getBoard, createList, updateList, deleteList,
  createCard, updateCard, deleteCard, moveCard
} from './api/client'
import './App.css'

const BOARD_ID = 1

export default function App() {
  const [board, setBoard] = useState(null)
  const [editingCard, setEditingCard] = useState(null)

  const loadBoard = useCallback(() => {
    getBoard(BOARD_ID).then(setBoard).catch(console.error)
  }, [])

  useEffect(() => { loadBoard() }, [loadBoard])

  async function handleAddList() {
    const list = await createList(BOARD_ID, '新しいリスト')
    setBoard(prev => ({ ...prev, lists: [...prev.lists, { ...list, cards: [] }] }))
  }

  async function handleDeleteList(listId) {
    await deleteList(listId)
    setBoard(prev => ({ ...prev, lists: prev.lists.filter(l => l.id !== listId) }))
  }

  async function handleRenameList(listId, title) {
    const updated = await updateList(listId, title)
    setBoard(prev => ({
      ...prev,
      lists: prev.lists.map(l => l.id === listId ? { ...l, title: updated.title } : l)
    }))
  }

  async function handleAddCard(listId) {
    const card = await createCard(listId)
    setBoard(prev => ({
      ...prev,
      lists: prev.lists.map(l =>
        l.id === listId ? { ...l, cards: [...l.cards, card] } : l
      )
    }))
    setEditingCard(card)
  }

  async function handleDeleteCard(cardId) {
    await deleteCard(cardId)
    setBoard(prev => ({
      ...prev,
      lists: prev.lists.map(l => ({ ...l, cards: l.cards.filter(c => c.id !== cardId) }))
    }))
  }

  async function handleSaveCard(data) {
    const updated = await updateCard(editingCard.id, data)
    setBoard(prev => ({
      ...prev,
      lists: prev.lists.map(l => ({
        ...l,
        cards: l.cards.map(c => c.id === updated.id ? updated : c)
      }))
    }))
    setEditingCard(null)
  }

  async function handleDragEnd(result) {
    const { source, destination, draggableId } = result
    if (!destination) return
    if (source.droppableId === destination.droppableId && source.index === destination.index) return

    const cardId = parseInt(draggableId)
    const toListId = parseInt(destination.droppableId)

    // Optimistic UI update
    setBoard(prev => {
      const lists = prev.lists.map(l => ({ ...l, cards: [...l.cards] }))
      const fromList = lists.find(l => l.id === parseInt(source.droppableId))
      const toList = lists.find(l => l.id === toListId)
      const [card] = fromList.cards.splice(source.index, 1)
      toList.cards.splice(destination.index, 0, card)
      return { ...prev, lists }
    })

    await moveCard(cardId, toListId, destination.index)
  }

  if (!board) return <div style={{ padding: 24, color: '#666' }}>読み込み中...</div>

  return (
    <>
      <header id="app-header">
        <h1 id="app-title">📋 タスク管理アプリ</h1>
        <button id="btn-add-list" onClick={handleAddList}>＋ リスト追加</button>
      </header>

      <DragDropContext onDragEnd={handleDragEnd}>
        <main id="board">
          {board.lists.map(list => (
            <List
              key={list.id}
              list={list}
              onAddCard={handleAddCard}
              onDeleteList={handleDeleteList}
              onRenameList={handleRenameList}
              onEditCard={setEditingCard}
              onDeleteCard={handleDeleteCard}
            />
          ))}
        </main>
      </DragDropContext>

      {editingCard && (
        <CardModal
          card={editingCard}
          onSave={handleSaveCard}
          onClose={() => setEditingCard(null)}
        />
      )}
    </>
  )
}
