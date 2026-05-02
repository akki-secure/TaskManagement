import { useState, useEffect, useCallback } from 'react'
import { DragDropContext } from '@hello-pangea/dnd'
import List from './components/List'
import CardModal from './components/CardModal'
import AccountSettingsModal from './components/AccountSettingsModal'
import TrashModal from './components/TrashModal'
import {
    getMyBoards, getBoard, createList, updateList, deleteList,
    createCard, updateCard, deleteCard, moveCard
} from './api/client'
import { useAuth } from './auth/AuthContext'
import './App.css'

export default function App() {
    const [board, setBoard] = useState(null)
    const [editingCard, setEditingCard] = useState(null)
    const [showSettings, setShowSettings] = useState(false)
    const [showTrash, setShowTrash] = useState(false)
    const { auth, logout } = useAuth()

    const loadBoard = useCallback(() => {
        getMyBoards()
            .then(boards => boards.length > 0 ? getBoard(boards[0].id) : null)
            .then(setBoard)
            .catch(console.error)
    }, [])

    useEffect(() => { loadBoard() }, [loadBoard])

    async function handleAddList() {
        if (!board) return
        const list = await createList(board.id, '新しいリスト')
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

    function handleRestoreCard(restoredCard) {
        setBoard(prev => ({
            ...prev,
            lists: prev.lists.map(l =>
                l.id === restoredCard.listId
                    ? { ...l, cards: [...l.cards, restoredCard].sort((a, b) => a.position - b.position) }
                    : l
            )
        }))
    }

    function handleRestoreList() {
        loadBoard()
    }

    async function handleDragEnd(result) {
        const { source, destination, draggableId } = result
        if (!destination) return
        if (source.droppableId === destination.droppableId && source.index === destination.index) return

        const cardId = parseInt(draggableId)
        const toListId = parseInt(destination.droppableId)

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
                <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                    <button
                        className="header-user-btn"
                        onClick={() => setShowSettings(true)}
                        title="アカウント設定"
                    >
                        {auth?.user?.username}
                    </button>
                    <button
                        className="header-ghost-btn"
                        onClick={logout}
                    >
                        ログアウト
                    </button>
                    <button
                        className="header-ghost-btn"
                        onClick={() => setShowTrash(true)}
                    >
                        🗑 ゴミ箱
                    </button>
                    <button id="btn-add-list" onClick={handleAddList}>＋ リスト追加</button>
                </div>
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

            {showSettings && (
                <AccountSettingsModal onClose={() => setShowSettings(false)} />
            )}

            {showTrash && (
                <TrashModal
                    onClose={() => setShowTrash(false)}
                    onRestoreCard={handleRestoreCard}
                    onRestoreList={handleRestoreList}
                />
            )}
        </>
    )
}
