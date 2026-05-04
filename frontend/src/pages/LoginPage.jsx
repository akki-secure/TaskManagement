import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { loginApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export default function LoginPage() {
    const [identifier, setIdentifier] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const { login } = useAuth()
    const navigate = useNavigate()

    async function handleSubmit(e) {
        e.preventDefault()
        setError('')
        setLoading(true)
        try {
            const data = await loginApi({ identifier, password })
            login(data.token, { id: data.userId, username: data.username, email: data.email })
            navigate('/')
        } catch (err) {
            setError(err.response?.data?.message || 'ログインに失敗しました')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="auth-page">
            <form className="auth-form" onSubmit={handleSubmit}>
                <h1 className="auth-title">ログイン</h1>
                {error && <p className="auth-error">{error}</p>}
                <input
                    className="auth-input"
                    type="text"
                    placeholder="ユーザー名またはメールアドレス"
                    value={identifier}
                    onChange={e => setIdentifier(e.target.value)}
                    required
                    autoComplete="username"
                />
                <input
                    className="auth-input"
                    type="password"
                    placeholder="パスワード"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                    autoComplete="current-password"
                />
                <button className="auth-btn-primary" type="submit" disabled={loading}>
                    {loading ? 'ログイン中...' : 'ログイン'}
                </button>
                <p className="auth-link">
                    アカウントをお持ちでない方は <Link to="/register">新規登録</Link>
                </p>
            </form>
        </div>
    )
}
