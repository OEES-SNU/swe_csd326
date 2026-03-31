import { useState, useEffect } from 'react'
import api from '../../api/axios'

export default function Enrollments() {
  const [students, setStudents] = useState([])
  const [courses, setCourses] = useState([])
  const [form, setForm] = useState({ studentId: '', courseId: '' })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    Promise.all([api.get('/admin/students'), api.get('/admin/courses')])
      .then(([sRes, cRes]) => {
        setStudents(sRes.data.filter((u) => u.role === 'STUDENT'))
        setCourses(cRes.data)
      })
      .catch(() => setError('Failed to load data.'))
      .finally(() => setLoading(false))
  }, [])

  const handleEnroll = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      await api.post('/admin/enroll', {
        studentId: Number(form.studentId),
        courseId: Number(form.courseId),
      })
      setSuccess('Student enrolled successfully.')
      setForm({ studentId: '', courseId: '' })
    } catch (err) {
      setError(err.response?.data?.message ?? 'Enrollment failed.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="text-sm text-gray-400 py-12 text-center">Loading…</div>

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Enrollments</h1>
          <p className="page-subtitle">Enroll students into courses</p>
        </div>
      </div>

      <div className="max-w-lg">
        <div className="card p-6">
          <h2 className="text-sm font-semibold text-gray-900 mb-4">Enroll a student</h2>

          {error && (
            <div className="mb-4 px-3 py-2.5 bg-red-50 border border-red-200 rounded text-sm text-red-700">
              {error}
            </div>
          )}
          {success && (
            <div className="mb-4 px-3 py-2.5 bg-green-50 border border-green-200 rounded text-sm text-green-700">
              {success}
            </div>
          )}

          <form onSubmit={handleEnroll} className="space-y-4">
            <div className="form-group">
              <label className="label">Student</label>
              <select className="select" required value={form.studentId}
                onChange={(e) => setForm({ ...form, studentId: e.target.value })}>
                <option value="">Select a student…</option>
                {students.map((s) => (
                  <option key={s.id} value={s.id}>{s.name} ({s.email})</option>
                ))}
              </select>
              {students.length === 0 && (
                <p className="text-xs text-gray-400 mt-1">No students registered yet.</p>
              )}
            </div>

            <div className="form-group mb-0">
              <label className="label">Course</label>
              <select className="select" required value={form.courseId}
                onChange={(e) => setForm({ ...form, courseId: e.target.value })}>
                <option value="">Select a course…</option>
                {courses.map((c) => (
                  <option key={c.id} value={c.id}>{c.courseCode} — {c.courseName}</option>
                ))}
              </select>
            </div>

            <div className="flex justify-end pt-2">
              <button type="submit" className="btn-primary" disabled={saving || !form.studentId || !form.courseId}>
                {saving ? 'Enrolling…' : 'Enroll student'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  )
}
