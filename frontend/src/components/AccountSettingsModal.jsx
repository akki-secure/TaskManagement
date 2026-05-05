import { useState } from 'react'
import { updateProfile, changePassword } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export default function AccountSettingsModal({ onClose }) {
    const { auth, updateUser } = useAuth()
    const [tab, setTab] = useState('profile')

    const [username, setUsername] = useState(auth?.user?.username || '')
    const [email, setEmail] = useState(auth?.user?.email || '')
    const [profileMsg, setProfileMsg] = useState(null)
    const [profileLoading, setProfileLoading] = useState(false)

    const [currentPassword, setCurrentPassword] = useState('')
    const [newPassword, setNewPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')
    const [showPassword, setShowPassword] = useState(false)
    const [passwordMsg, setPasswordMsg] = useState(null)
    const [passwordLoading, setPasswordLoading] = useState(false)

    async function handleProfileSave(e) {
        e.preventDefault()
        setProfileMsg(null)
        if (!username.trim().match(/^[a-zA-Z0-9_ 　々぀-ゟ゠-ヿ一-鿿㐀-䶿]{2,50}$/u)) {
            setProfileMsg({ type: 'error', text: 'ユーザー名は2〜50文字で入力してください（英数字・アンダースコア・スペース・日本語が使えます）' })
            return
        }
        setProfileLoading(true)
        try {
            const data = await updateProfile({ username, email })
            updateUser(data.token, { id: data.userId, username: data.username, email: data.email })
            setProfileMsg({ type: 'success', text: 'プロフィールを更新しました' })
        } catch (err) {
            setProfileMsg({ type: 'error', text: err.response?.data?.message || '更新に失敗しました' })
        } finally {
            setProfileLoading(false)
        }
    }

    async function handlePasswordSave(e) {
        e.preventDefault()
        setPasswordMsg(null)
        if (!/^[a-zA-Z0-9]{8,}$/.test(newPassword)) {
            setPasswordMsg({ type: 'error', text: 'パスワードは英数字のみ8文字以上で入力してください' })
            return
        }
        if (newPassword !== confirmPassword) {
            setPasswordMsg({ type: 'error', text: '新しいパスワードが一致しません' })
            return
        }
        setPasswordLoading(true)
        try {
            await changePassword({ currentPassword, newPassword })
            setPasswordMsg({ type: 'success', text: 'パスワードを変更しました' })
            setCurrentPassword('')
            setNewPassword('')
            setConfirmPassword('')
        } catch (err) {
            setPasswordMsg({ type: 'error', text: err.response?.data?.message || 'パスワード変更に失敗しました' })
        } finally {
            setPasswordLoading(false)
        }
    }

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-box account-settings-modal" onClick={e => e.stopPropagation()}>
                <div className="settings-modal-header">
                    <h2 className="settings-modal-title">アカウント設定</h2>
                    <button className="settings-modal-close" onClick={onClose}>✕</button>
                </div>

                <div className="settings-tabs">
                    <button
                        className={`settings-tab${tab === 'profile' ? ' active' : ''}`}
                        onClick={() => setTab('profile')}
                    >
                        プロフィール
                    </button>
                    <button
                        className={`settings-tab${tab === 'password' ? ' active' : ''}`}
                        onClick={() => setTab('password')}
                    >
                        パスワード変更
                    </button>
                </div>

                {tab === 'profile' && (
                    <form className="settings-form" onSubmit={handleProfileSave}>
                        {profileMsg && (
                            <p className={profileMsg.type === 'success' ? 'settings-success' : 'auth-error'}>
                                {profileMsg.text}
                            </p>
                        )}
                        <label className="settings-label">ユーザー名<span className="required-mark">※</span> <span className="settings-hint">（2〜50文字・スペース・日本語も使用可）</span></label>
                        <input
                            className="auth-input"
                            type="text"
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                            required
                            minLength={2}
                            maxLength={50}
                            autoComplete="username"
                        />
                        <label className="settings-label">メールアドレス<span className="required-mark">※</span></label>
                        <input
                            className="auth-input"
                            type="email"
                            value={email}
                            onChange={e => setEmail(e.target.value)}
                            required
                            autoComplete="email"
                        />
                        <button className="auth-btn-primary" type="submit" disabled={profileLoading}>
                            {profileLoading ? '更新中...' : '変更を保存'}
                        </button>
                    </form>
                )}

                {tab === 'password' && (
                    <form className="settings-form" onSubmit={handlePasswordSave}>
                        {passwordMsg && (
                            <p className={passwordMsg.type === 'success' ? 'settings-success' : 'auth-error'}>
                                {passwordMsg.text}
                            </p>
                        )}
                        <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 4 }}>
                            <button
                                type="button"
                                className="auth-pw-toggle"
                                onClick={() => setShowPassword(v => !v)}
                                tabIndex={-1}
                            >
                                {showPassword ? '非表示' : '表示'}
                            </button>
                        </div>
                        <label className="settings-label">現在のパスワード<span className="required-mark">※</span></label>
                        <input
                            className="auth-input"
                            type={showPassword ? 'text' : 'password'}
                            value={currentPassword}
                            onChange={e => setCurrentPassword(e.target.value)}
                            required
                            autoComplete="current-password"
                        />
                        <label className="settings-label">新しいパスワード（英数字8文字以上）<span className="required-mark">※</span></label>
                        <input
                            className="auth-input"
                            type={showPassword ? 'text' : 'password'}
                            value={newPassword}
                            onChange={e => setNewPassword(e.target.value)}
                            required
                            autoComplete="new-password"
                        />
                        <label className="settings-label">新しいパスワード（確認）<span className="required-mark">※</span></label>
                        <input
                            className="auth-input"
                            type={showPassword ? 'text' : 'password'}
                            value={confirmPassword}
                            onChange={e => setConfirmPassword(e.target.value)}
                            required
                            autoComplete="new-password"
                        />
                        <button className="auth-btn-primary" type="submit" disabled={passwordLoading}>
                            {passwordLoading ? '変更中...' : 'パスワードを変更'}
                        </button>
                    </form>
                )}
            </div>
        </div>
    )
}
