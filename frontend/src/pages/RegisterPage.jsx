import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export default function RegisterPage() {
    const [username, setUsername] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const { login } = useAuth()
    const navigate = useNavigate()

    async function handleSubmit(e) {
        e.preventDefault()
        setError('')
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
                <input
                    className="auth-input"
                    type="text"
                    placeholder="ユーザー名（3〜50文字・日本語も使用可）"
                    value={username}
                    onChange={e => setUsername(e.target.value)}
                    required
                    autoComplete="username"
                />
                <input
                    className="auth-input"
                    type="email"
                    placeholder="メールアドレス"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    required
                    autoComplete="email"
                />
                <input
                    className="auth-input"
                    type="password"
                    placeholder="パスワード（8文字以上）"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                    minLength={8}
                    autoComplete="new-password"
                />
                <input
                    className="auth-input"
                    type="password"
                    placeholder="パスワード（確認用）"
                    value={confirmPassword}
                    onChange={e => setConfirmPassword(e.target.value)}
                    required
                    minLength={8}
                    autoComplete="new-password"
                />
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
