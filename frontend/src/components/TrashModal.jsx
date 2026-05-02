import { useState, useEffect } from 'react'
import {
    getTrashedCards,
    getTrashedLists,
    restoreCard,
    restoreList,
    deleteCardPermanently,
    deleteListPermanently,
} from '../api/client'

function formatDate(isoString) {
    if (!isoString) return ''
    return new Date(isoString).toLocaleDateString('ja-JP', {
        year: 'numeric', month: 'short', day: 'numeric',
    })
}

export default function TrashModal({ onClose, onRestoreCard, onRestoreList }) {
    const [tab, setTab] = useState('cards')
    const [cards, setCards] = useState([])
    const [lists, setLists] = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        Promise.all([getTrashedCards(), getTrashedLists()])
            .then(([c, l]) => { setCards(c); setLists(l) })
            .finally(() => setLoading(false))
    }, [])

    async function handleRestoreCard(id) {
        const restored = await restoreCard(id)
        setCards(prev => prev.filter(c => c.id !== id))
        onRestoreCard(restored)
    }

    async function handleDeleteCardPermanently(id) {
        if (!window.confirm('このカードを完全に削除しますか？この操作は取り消せません。')) return
        await deleteCardPermanently(id)
        setCards(prev => prev.filter(c => c.id !== id))
    }

    async function handleRestoreList(id) {
        const restored = await restoreList(id)
        setLists(prev => prev.filter(l => l.id !== id))
        onRestoreList(restored)
    }

    async function handleDeleteListPermanently(id) {
        if (!window.confirm('このリストとカードを完全に削除しますか？この操作は取り消せません。')) return
        await deleteListPermanently(id)
        setLists(prev => prev.filter(l => l.id !== id))
    }

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" style={{ width: 560 }} onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <h2 className="modal-title">ゴミ箱</h2>
                    <button className="modal-close" onClick={onClose}>✕</button>
                </div>

                <div className="trash-tabs">
                    <button
                        className={`trash-tab${tab === 'cards' ? ' active' : ''}`}
                        onClick={() => setTab('cards')}
                    >
                        カード {cards.length > 0 && `(${cards.length})`}
                    </button>
                    <button
                        className={`trash-tab${tab === 'lists' ? ' active' : ''}`}
                        onClick={() => setTab('lists')}
                    >
                        リスト {lists.length > 0 && `(${lists.length})`}
                    </button>
                </div>

                <div className="trash-list">
                    {loading ? (
                        <p className="trash-empty">読み込み中...</p>
                    ) : tab === 'cards' ? (
                        cards.length === 0 ? (
                            <p className="trash-empty">ゴミ箱にカードはありません</p>
                        ) : (
                            cards.map(card => (
                                <div key={card.id} className="trash-item">
                                    <div className="trash-item-info">
                                        <div className="trash-item-title">{card.title}</div>
                                        <div className="trash-item-meta">削除日: {formatDate(card.deletedAt)}</div>
                                    </div>
                                    <div className="trash-item-actions">
                                        <button
                                            className="btn-restore"
                                            onClick={() => handleRestoreCard(card.id)}
                                        >
                                            元に戻す
                                        </button>
                                        <button
                                            className="btn-delete-permanent"
                                            onClick={() => handleDeleteCardPermanently(card.id)}
                                        >
                                            完全削除
                                        </button>
                                    </div>
                                </div>
                            ))
                        )
                    ) : (
                        lists.length === 0 ? (
                            <p className="trash-empty">ゴミ箱にリストはありません</p>
                        ) : (
                            lists.map(list => (
                                <div key={list.id} className="trash-item">
                                    <div className="trash-item-info">
                                        <div className="trash-item-title">{list.title}</div>
                                        <div className="trash-item-meta">削除日: {formatDate(list.deletedAt)}</div>
                                    </div>
                                    <div className="trash-item-actions">
                                        <button
                                            className="btn-restore"
                                            onClick={() => handleRestoreList(list.id)}
                                        >
                                            元に戻す
                                        </button>
                                        <button
                                            className="btn-delete-permanent"
                                            onClick={() => handleDeleteListPermanently(list.id)}
                                        >
                                            完全削除
                                        </button>
                                    </div>
                                </div>
                            ))
                        )
                    )}
                </div>
            </div>
        </div>
    )
}
