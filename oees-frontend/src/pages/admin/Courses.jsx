import { useState, useEffect } from 'react'
import api from '../../api/axios'
import Modal from '../../components/Modal'

export default function Courses() {
  const [courses, setCourses] = useState([])
  const [students, setStudents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [createOpen, setCreateOpen] = useState(false)
  const [assignOpen, setAssignOpen] = useState(null) // holds course
  const [createForm, setCreateForm] = useState({ courseCode: '', courseName: '', description: '' })
  const [assignForm, setAssignForm] = useState({ instructorId: '' })
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const load = async () => {
    try {
      const [cRes, sRes] = await Promise.all([
        api.get('/admin/courses'),
        api.get('/admin/instructors'),
      ])
      setCourses(cRes.data)
      setStudents(sRes.data)
    } catch {
      setError('Failed to load data.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      await api.post('/admin/courses', createForm)
      setCreateOpen(false)
      setCreateForm({ courseCode: '', courseName: '', description: '' })
      load()
    } catch (err) {
      setFormError(err.response?.data?.message ?? 'Failed to create course.')
    } finally {
      setSaving(false)
    }
  }

  const handleAssign = async (e) => {
    e.preventDefault()
    if (!assignForm.instructorId) return
    setSaving(true)
    setFormError('')
    try {
      await api.put(`/admin/courses/${assignOpen.id}/assign-instructor/${assignForm.instructorId}`)
      setAssignOpen(null)
      setAssignForm({ instructorId: '' })
      load()
    } catch (err) {
      setFormError(err.response?.data?.message ?? 'Failed to assign instructor.')
    } finally {
      setSaving(false)
    }
  }

  const instructors = students

  const statusBadge = (course) => {
    if (course.instructor) return <span className="badge-green">Assigned</span>
    return <span className="badge-yellow">Unassigned</span>
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Courses</h1>
          <p className="page-subtitle">Manage courses and instructor assignments</p>
        </div>
        <button className="btn-primary" onClick={() => setCreateOpen(true)}>
          Add course
        </button>
      </div>

      {error && (
        <div className="mb-4 px-3 py-2.5 bg-red-50 border border-red-200 rounded text-sm text-red-700">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-sm text-gray-400 py-12 text-center">Loading…</div>
      ) : (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Description</th>
                <th>Instructor</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {courses.length === 0 && (
                <tr>
                  <td colSpan={6} className="text-center text-gray-400 py-10">
                    No courses yet. Create one to get started.
                  </td>
                </tr>
              )}
              {courses.map((c) => (
                <tr key={c.id}>
                  <td className="font-mono text-xs">{c.courseCode}</td>
                  <td className="font-medium text-gray-900">{c.courseName}</td>
                  <td className="text-gray-500 max-w-xs truncate">{c.description || '—'}</td>
                  <td>{c.instructor?.name ?? <span className="text-gray-400">—</span>}</td>
                  <td>{statusBadge(c)}</td>
                  <td className="text-right">
                    <button
                      className="btn-secondary btn-sm"
                      onClick={() => { setAssignOpen(c); setFormError('') }}
                    >
                      Assign instructor
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Create Course Modal */}
      <Modal open={createOpen} onClose={() => { setCreateOpen(false); setFormError('') }} title="Create Course">
        <form onSubmit={handleCreate} className="space-y-4">
          {formError && (
            <div className="px-3 py-2.5 bg-red-50 border border-red-200 rounded text-sm text-red-700">
              {formError}
            </div>
          )}
          <div className="form-group">
            <label className="label">Course code</label>
            <input className="input" placeholder="e.g. CS101" required value={createForm.courseCode}
              onChange={(e) => setCreateForm({ ...createForm, courseCode: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="label">Course name</label>
            <input className="input" placeholder="e.g. Introduction to Computing" required value={createForm.courseName}
              onChange={(e) => setCreateForm({ ...createForm, courseName: e.target.value })} />
          </div>
          <div className="form-group mb-0">
            <label className="label">Description</label>
            <textarea className="input resize-none" rows={3} placeholder="Optional description"
              value={createForm.description}
              onChange={(e) => setCreateForm({ ...createForm, description: e.target.value })} />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" className="btn-secondary" onClick={() => setCreateOpen(false)}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Create'}</button>
          </div>
        </form>
      </Modal>

      {/* Assign Instructor Modal */}
      <Modal open={!!assignOpen} onClose={() => { setAssignOpen(null); setFormError('') }} title={`Assign Instructor — ${assignOpen?.courseName}`}>
        <form onSubmit={handleAssign} className="space-y-4">
          {formError && (
            <div className="px-3 py-2.5 bg-red-50 border border-red-200 rounded text-sm text-red-700">
              {formError}
            </div>
          )}
          <div className="form-group mb-0">
            <label className="label">Instructor</label>
            <select className="select" required value={assignForm.instructorId}
              onChange={(e) => setAssignForm({ instructorId: e.target.value })}>
              <option value="">Select an instructor…</option>
              {instructors.map((i) => (
                <option key={i.id} value={i.id}>{i.name} ({i.email})</option>
              ))}
            </select>
            {instructors.length === 0 && (
              <p className="text-xs text-gray-400 mt-1">No instructors registered yet.</p>
            )}
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" className="btn-secondary" onClick={() => setAssignOpen(null)}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={saving || !assignForm.instructorId}>
              {saving ? 'Assigning…' : 'Assign'}
            </button>
          </div>
        </form>
      </Modal>
    </>
  )
}
