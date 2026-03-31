import { useState, useEffect } from 'react'
import api from '../../api/axios'
import Modal from '../../components/Modal'

const roleBadge = (role) => {
  if (role === 'ADMIN') return <span className="badge-gray">Admin</span>
  if (role === 'INSTRUCTOR') return <span className="badge-blue">Instructor</span>
  return <span className="badge-green">Student</span>
}

const EMPTY_FORM = { name: '', email: '', password: '', role: 'STUDENT' }

export default function Students() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const load = () => {
    api.get('/admin/users')
      .then((r) => setUsers(r.data))
      .catch(() => setError('Failed to load users.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      await api.post('/auth/register', form)
      setModalOpen(false)
      setForm(EMPTY_FORM)
      load()
    } catch (err) {
      setFormError(err.response?.data?.message ?? 'Failed to create user.')
    } finally {
      setSaving(false)
    }
  }

  const filtered = users.filter((u) =>
    u.name?.toLowerCase().includes(search.toLowerCase()) ||
    u.email?.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Users</h1>
          <p className="page-subtitle">All registered users on the platform</p>
        </div>
        <button className="btn-primary" onClick={() => { setForm(EMPTY_FORM); setFormError(''); setModalOpen(true) }}>
          Create user
        </button>
      </div>

      {error && (
        <div className="mb-4 px-3 py-2.5 bg-red-50 border border-red-200 rounded text-sm text-red-700">{error}</div>
      )}

      <div className="mb-4">
        <input
          type="text"
          className="input max-w-xs"
          placeholder="Search by name or email…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {loading ? (
        <div className="text-sm text-gray-400 py-12 text-center">Loading…</div>
      ) : (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 && (
                <tr>
                  <td colSpan={4} className="text-center text-gray-400 py-10">
                    {search ? 'No users match your search.' : 'No users found.'}
                  </td>
                </tr>
              )}
              {filtered.map((u) => (
                <tr key={u.id}>
                  <td className="font-medium text-gray-900">{u.name}</td>
                  <td className="text-gray-500">{u.email}</td>
                  <td>{roleBadge(u.role)}</td>
                  <td>
                    {u.active !== false
                      ? <span className="badge-green">Active</span>
                      : <span className="badge-red">Inactive</span>
                    }
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Create User">
        <form onSubmit={handleCreate} className="space-y-4">
          {formError && (
            <div className="px-3 py-2.5 bg-red-50 border border-red-200 rounded text-sm text-red-700">{formError}</div>
          )}
          <div className="form-group">
            <label className="label">Full name</label>
            <input className="input" placeholder="John Doe" required value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="label">Email address</label>
            <input type="email" className="input" placeholder="john@example.com" required value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="label">Password</label>
            <input type="password" className="input" placeholder="Min. 8 characters" required value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </div>
          <div className="form-group mb-0">
            <label className="label">Role</label>
            <select className="select" value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value })}>
              <option value="STUDENT">Student</option>
              <option value="INSTRUCTOR">Instructor</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? 'Creating…' : 'Create user'}
            </button>
          </div>
        </form>
      </Modal>
    </>
  )
}
