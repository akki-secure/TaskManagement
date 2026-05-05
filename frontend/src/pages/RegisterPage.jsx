import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export default function RegisterPage() {
    const [username, setUsername] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')
    const [showPassword, setShowPassword] = useState(false)
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const { login } = useAuth()
    const navigate = useNavigate()

    async function handleSubmit(e) {
        e.preventDefault()
        setError('')
        if (!username.trim()) {
            setError('ユーザー名を入力してください')
            return
        }
        if (!/^[a-zA-Z0-9]{8,}$/.test(password)) {
            setError('パスワードは英数字のみ8文字以上で入力してください')
            return
        }
        if (password !== confirmPassword) {
            setError('パスワードが一致しません')
            return
        }
        setLoading(true)
        try {
            const data = await register({ username, email, password })
            login(data.token, { id: data.userId, username: data.username, email: data.email })
            navigate('/')
        } catch (err) {
            setError(err.response?.data?.message || '登録に失敗しました')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="auth-page">
            <form className="auth-form" onSubmit={handleSubmit}>
                <h1 className="auth-title">新規登録</h1>
                {error && <p className="auth-error">{error}</p>}
                <div className="auth-field">
                    <label className="auth-label">ユーザー名<span className="required-mark">※</span></label>
                    <input
                        className="auth-input"
                        type="text"
                        value={username}
                        onChange={e => setUsername(e.target.value)}
                        required
                        autoComplete="username"
                    />
                </div>
                <div className="auth-field">
                    <label className="auth-label">メールアドレス<span className="required-mark">※</span></label>
                    <input
                        className="auth-input"
                        type="email"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        required
                        autoComplete="email"
                    />
                </div>
                <div className="auth-field">
                    <label className="auth-label">パスワード（英数字8文字以上）<span className="required-mark">※</span></label>
                    <div className="auth-password-wrap">
                        <input
                            className="auth-input"
                            type={showPassword ? 'text' : 'password'}
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            required
                            minLength={8}
                            autoComplete="new-password"
                        />
                        <button
                            type="button"
                            className="auth-pw-toggle"
                            onClick={() => setShowPassword(v => !v)}
                            tabIndex={-1}
                        >
                            {showPassword ? '非表示' : '表示'}
                        </button>
                    </div>
                </div>
                <div className="auth-field">
                    <label className="auth-label">パスワード（確認）<span className="required-mark">※</span></label>
                    <div className="auth-password-wrap">
                        <input
                            className="auth-input"
                            type={showPassword ? 'text' : 'password'}
                            value={confirmPassword}
                            onChange={e => setConfirmPassword(e.target.value)}
                            required
                            minLength={8}
                            autoComplete="new-password"
                        />
                    </div>
                </div>
                <button className="auth-btn-primary" type="submit" disabled={loading}>
                    {loading ? '登録中...' : '登録する'}
                </button>
                <p className="auth-link">
                    すでにアカウントをお持ちの方は <Link to="/login">ログイン</Link>
                </p>
            </form>
        </div>
    )
}
