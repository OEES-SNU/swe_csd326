import { useState, useEffect } from 'react'
import api from '../../api/axios'
import Modal from '../../components/Modal'

const difficultyBadge = (d) => {
  if (d === 'EASY') return <span className="badge-green">Easy</span>
  if (d === 'MEDIUM') return <span className="badge-yellow">Medium</span>
  return <span className="badge-red">Hard</span>
}

const typeBadge = (t) => {
  if (t === 'MULTIPLE_CHOICE') return <span className="badge-blue">MCQ</span>
  if (t === 'FILL_IN_THE_BLANK') return <span className="badge-gray">Fill-in</span>
  return <span className="badge-gray">Descriptive</span>
}

const EMPTY_FORM = {
  content: '',
  type: 'MULTIPLE_CHOICE',
  difficultyLevel: 'EASY',
  marks: '',
  unit: '',
  optionA: '', optionB: '', optionC: '', optionD: '',
  correctAnswer: '',
  courseId: '',
}

export default function Questions() {
  const [courses, setCourses] = useState([])
  const [selectedCourse, setSelectedCourse] = useState('')
  const [questions, setQuestions] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [editTarget, setEditTarget] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  useEffect(() => {
    api.get('/instructor/questions/my-courses')
      .then((r) => {
        setCourses(r.data)
        if (r.data.length > 0) setSelectedCourse(String(r.data[0].id))
      })
      .catch((err) => {
        if (err.response?.status === 403) {
          alert('Access denied: your account does not have the Instructor role. Contact your administrator.')
        }
      })
  }, [])

  useEffect(() => {
    if (!selectedCourse) return
    setLoading(true)
    api.get(`/instructor/questions/course/${selectedCourse}`)
      .then((r) => setQuestions(r.data))
      .catch(() => setQuestions([]))
      .finally(() => setLoading(false))
  }, [selectedCourse])

  const openCreate = () => {
    setEditTarget(null)
    setForm({ ...EMPTY_FORM, courseId: selectedCourse })
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (q) => {
    setEditTarget(q)
    setForm({
      content: q.content,
      type: q.type,
      difficultyLevel: q.difficultyLevel,
      marks: String(q.marks),
      unit: q.unit ?? '',
      optionA: q.optionA ?? '', optionB: q.optionB ?? '',
      optionC: q.optionC ?? '', optionD: q.optionD ?? '',
      correctAnswer: q.correctAnswer ?? '',
      courseId: selectedCourse,
    })
    setFormError('')
    setModalOpen(true)
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete this question?')) return
    try {
      await api.delete(`/instructor/questions/${id}`)
      setQuestions((prev) => prev.filter((q) => q.id !== id))
    } catch {
      alert('Failed to delete question.')
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    const payload = {
      ...form,
      marks: Number(form.marks),
      courseId: Number(form.courseId),
    }
    try {
      if (editTarget) {
        await api.put(`/instructor/questions/${editTarget.id}`, payload)
      } else {
        await api.post('/instructor/questions', payload)
      }
      setModalOpen(false)
      setLoading(true)
      api.get(`/instructor/questions/course/${selectedCourse}`)
        .then((r) => setQuestions(r.data))
        .finally(() => setLoading(false))
    } catch (err) {
      setFormError(err.response?.data?.message ?? 'Failed to save question.')
    } finally {
      setSaving(false)
    }
  }

  const isMCQ = form.type === 'MULTIPLE_CHOICE'

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Question Bank</h1>
          <p className="page-subtitle">Create and manage exam questions</p>
        </div>
        <button className="btn-primary" onClick={openCreate}>Add question</button>
      </div>

      <div className="flex items-center gap-3 mb-5">
        <label className="text-sm font-medium text-gray-700">Course:</label>
        <select className="select max-w-xs" value={selectedCourse} onChange={(e) => setSelectedCourse(e.target.value)}>
          {courses.map((c) => (
            <option key={c.id} value={c.id}>{c.courseCode} — {c.courseName}</option>
          ))}
        </select>
        <span className="text-sm text-gray-400">{questions.length} questions</span>
      </div>

      {loading ? (
        <div className="text-sm text-gray-400 py-12 text-center">Loading…</div>
      ) : (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Question</th>
                <th>Type</th>
                <th>Difficulty</th>
                <th>Unit</th>
                <th>Marks</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {questions.length === 0 && (
                <tr>
                  <td colSpan={6} className="text-center text-gray-400 py-10">
                    No questions for this course yet.
                  </td>
                </tr>
              )}
              {questions.map((q) => (
                <tr key={q.id}>
                  <td className="max-w-xs">
                    <p className="truncate text-gray-900">{q.content}</p>
                  </td>
                  <td>{typeBadge(q.type)}</td>
                  <td>{difficultyBadge(q.difficultyLevel)}</td>
                  <td className="text-gray-500">{q.unit || '—'}</td>
                  <td className="text-gray-700 font-medium">{q.marks}</td>
                  <td className="text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button className="btn-secondary btn-sm" onClick={() => openEdit(q)}>Edit</button>
                      <button className="btn-danger btn-sm" onClick={() => handleDelete(q.id)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editTarget ? 'Edit Question' : 'Add Question'}
        width="max-w-2xl"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {formError && (
            <div className="px-3 py-2.5 bg-red-50 border border-red-200 rounded text-sm text-red-700">
              {formError}
            </div>
          )}

          <div className="form-group">
            <label className="label">Question</label>
            <textarea className="input resize-none" rows={3} required value={form.content}
              onChange={(e) => setForm({ ...form, content: e.target.value })} />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="form-group mb-0">
              <label className="label">Type</label>
              <select className="select" value={form.type}
                onChange={(e) => setForm({ ...form, type: e.target.value })}>
                <option value="MULTIPLE_CHOICE">Multiple Choice</option>
                <option value="FILL_IN_THE_BLANK">Fill in the Blank</option>
                <option value="DESCRIPTIVE">Descriptive</option>
              </select>
            </div>
            <div className="form-group mb-0">
              <label className="label">Difficulty</label>
              <select className="select" value={form.difficultyLevel}
                onChange={(e) => setForm({ ...form, difficultyLevel: e.target.value })}>
                <option value="EASY">Easy</option>
                <option value="MEDIUM">Medium</option>
                <option value="HARD">Hard</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="form-group mb-0">
              <label className="label">Unit / Topic</label>
              <input className="input" placeholder="e.g. Unit 3" value={form.unit}
                onChange={(e) => setForm({ ...form, unit: e.target.value })} />
            </div>
            <div className="form-group mb-0">
              <label className="label">Marks</label>
              <input type="number" min="1" className="input" required value={form.marks}
                onChange={(e) => setForm({ ...form, marks: e.target.value })} />
            </div>
          </div>

          {isMCQ && (
            <>
              <div className="grid grid-cols-2 gap-4">
                <div className="form-group mb-0">
                  <label className="label">Option A</label>
                  <input className="input" required={isMCQ} value={form.optionA}
                    onChange={(e) => setForm({ ...form, optionA: e.target.value })} />
                </div>
                <div className="form-group mb-0">
                  <label className="label">Option B</label>
                  <input className="input" required={isMCQ} value={form.optionB}
                    onChange={(e) => setForm({ ...form, optionB: e.target.value })} />
                </div>
                <div className="form-group mb-0">
                  <label className="label">Option C</label>
                  <input className="input" value={form.optionC}
                    onChange={(e) => setForm({ ...form, optionC: e.target.value })} />
                </div>
                <div className="form-group mb-0">
                  <label className="label">Option D</label>
                  <input className="input" value={form.optionD}
                    onChange={(e) => setForm({ ...form, optionD: e.target.value })} />
                </div>
              </div>
              <div className="form-group mb-0">
                <label className="label">Correct Answer (A / B / C / D)</label>
                <input className="input" placeholder="A" required={isMCQ} value={form.correctAnswer}
                  onChange={(e) => setForm({ ...form, correctAnswer: e.target.value.toUpperCase() })} />
              </div>
            </>
          )}

          {!isMCQ && (
            <div className="form-group mb-0">
              <label className="label">Answer / Key (optional)</label>
              <input className="input" value={form.correctAnswer}
                onChange={(e) => setForm({ ...form, correctAnswer: e.target.value })} />
            </div>
          )}

          <div className="flex justify-end gap-2 pt-2">
            <button type="button" className="btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? 'Saving…' : editTarget ? 'Update' : 'Add question'}
            </button>
          </div>
        </form>
      </Modal>
    </>
  )
}
