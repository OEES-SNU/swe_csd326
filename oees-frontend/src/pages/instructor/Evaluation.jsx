import { useState, useEffect } from 'react'
import api from '../../api/axios'

const fmt = (dt) => dt ? new Date(dt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : '—'

export default function Evaluation() {
  const [courses, setCourses] = useState([])
  const [selectedCourse, setSelectedCourse] = useState('')
  const [exams, setExams] = useState([])
  const [selectedExam, setSelectedExam] = useState(null)
  const [attempts, setAttempts] = useState([])
  const [selectedAttempt, setSelectedAttempt] = useState(null)
  const [responses, setResponses] = useState([])
  const [grades, setGrades] = useState({})
  const [saving, setSaving] = useState({})
  const [saved, setSaved] = useState({})
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/instructor/questions/my-courses').then((r) => {
      setCourses(r.data)
      if (r.data.length > 0) setSelectedCourse(String(r.data[0].id))
    })
  }, [])

  useEffect(() => {
    if (!selectedCourse) return
    setSelectedExam(null)
    setAttempts([])
    setSelectedAttempt(null)
    setResponses([])
    api.get(`/exams/course/${selectedCourse}`)
      .then((r) => setExams(r.data))
      .catch(() => setExams([]))
  }, [selectedCourse])

  const loadAttempts = async (exam) => {
    setSelectedExam(exam)
    setSelectedAttempt(null)
    setResponses([])
    setError('')
    setLoading(true)
    try {
      const { data } = await api.get(`/evaluation/exam/${exam.id}/attempts`)
      setAttempts(data)
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to load attempts.')
      setAttempts([])
    } finally {
      setLoading(false)
    }
  }

  const loadResponses = async (attempt) => {
    setSelectedAttempt(attempt)
    setGrades({})
    setSaved({})
    setError('')
    setLoading(true)
    try {
      const { data } = await api.get(`/evaluation/attempt/${attempt.id}/pending`)
      setResponses(data)
      if (data.length === 0) setError('No pending responses for this attempt.')
    } catch {
      setError('Failed to load responses.')
    } finally {
      setLoading(false)
    }
  }

  const handleGrade = async (responseId) => {
    const marks = grades[responseId]
    if (marks === undefined || marks === '') return
    setSaving((p) => ({ ...p, [responseId]: true }))
    try {
      await api.post('/evaluation/grade', {
        responseId: Number(responseId),
        marksAwarded: Number(marks),
      })
      setSaved((p) => ({ ...p, [responseId]: true }))
    } catch (err) {
      alert(err.response?.data?.message ?? 'Failed to save grade.')
    } finally {
      setSaving((p) => ({ ...p, [responseId]: false }))
    }
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Evaluation</h1>
          <p className="page-subtitle">Grade descriptive and fill-in-the-blank responses</p>
        </div>
      </div>

      {/* Course selector */}
      <div className="flex items-center gap-3 mb-6">
        <label className="text-sm font-medium text-gray-700">Course:</label>
        <select className="select max-w-xs" value={selectedCourse}
          onChange={(e) => setSelectedCourse(e.target.value)}>
          {courses.map((c) => (
            <option key={c.id} value={c.id}>{c.courseCode} — {c.courseName}</option>
          ))}
        </select>
      </div>

      <div className="flex gap-6 min-h-0">
        {/* Exam list */}
        <div className="w-64 flex-shrink-0">
          <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Exams</p>
          <div className="card divide-y divide-gray-100">
            {exams.length === 0 && (
              <p className="text-sm text-gray-400 p-4">No exams found.</p>
            )}
            {exams.map((ex) => (
              <button
                key={ex.id}
                onClick={() => loadAttempts(ex)}
                className={`w-full text-left px-4 py-3 transition-colors hover:bg-gray-50 ${selectedExam?.id === ex.id ? 'bg-gray-50 border-l-2 border-gray-900' : ''}`}
              >
                <p className={`text-sm font-medium ${selectedExam?.id === ex.id ? 'text-gray-900' : 'text-gray-700'}`}>
                  {ex.title}
                </p>
                <p className="text-xs text-gray-400 mt-0.5">{ex.questionCount} questions · {ex.totalMarks} marks</p>
              </button>
            ))}
          </div>
        </div>

        {/* Attempts list */}
        <div className="w-64 flex-shrink-0">
          <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">
            Submitted Attempts
          </p>
          <div className="card divide-y divide-gray-100">
            {!selectedExam && (
              <p className="text-sm text-gray-400 p-4">Select an exam.</p>
            )}
            {selectedExam && loading && !selectedAttempt && (
              <p className="text-sm text-gray-400 p-4">Loading…</p>
            )}
            {selectedExam && !loading && attempts.length === 0 && (
              <p className="text-sm text-gray-400 p-4">No submitted attempts yet.</p>
            )}
            {attempts.map((a) => (
              <button
                key={a.id}
                onClick={() => loadResponses(a)}
                className={`w-full text-left px-4 py-3 transition-colors hover:bg-gray-50 ${selectedAttempt?.id === a.id ? 'bg-gray-50 border-l-2 border-gray-900' : ''}`}
              >
                <p className={`text-sm font-medium ${selectedAttempt?.id === a.id ? 'text-gray-900' : 'text-gray-700'}`}>
                  {a.student?.name ?? `Student #${a.id}`}
                </p>
                <p className="text-xs text-gray-400 mt-0.5">
                  Attempt #{a.attemptNumber} · {fmt(a.submittedAt)}
                </p>
              </button>
            ))}
          </div>
        </div>

        {/* Responses */}
        <div className="flex-1 min-w-0">
          <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">
            Responses
            {selectedAttempt && (
              <span className="ml-2 normal-case font-normal text-gray-400">
                — {selectedAttempt.student?.name}, Attempt #{selectedAttempt.attemptNumber}
              </span>
            )}
          </p>

          {!selectedAttempt && (
            <div className="card p-8 text-center text-sm text-gray-400">
              Select an attempt to review responses.
            </div>
          )}

          {selectedAttempt && loading && (
            <div className="text-sm text-gray-400 py-8 text-center">Loading…</div>
          )}

          {error && !loading && (
            <div className="px-3 py-2.5 bg-red-50 border border-red-200 rounded text-sm text-red-700 mb-4">
              {error}
            </div>
          )}

          {!loading && responses.length > 0 && (
            <div className="space-y-4">
              {responses.map((r) => (
                <div key={r.responseId} className="card p-5">
                  <div className="flex items-start justify-between gap-4 mb-3">
                    <div>
                      <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">
                        {r.questionType?.replace(/_/g, ' ')} · {r.maxMarks} marks
                      </p>
                      <p className="text-sm font-medium text-gray-900">{r.questionContent}</p>
                    </div>
                    {saved[r.responseId] && <span className="badge-green flex-shrink-0">Graded</span>}
                  </div>

                  <div className="bg-gray-50 border border-gray-100 rounded p-3 mb-4">
                    <p className="text-xs text-gray-400 mb-1">Student response</p>
                    <p className="text-sm text-gray-700 whitespace-pre-wrap">
                      {r.responseText || <em className="text-gray-400">No response</em>}
                    </p>
                  </div>

                  <div className="flex items-center gap-3">
                    <input
                      type="number" min="0" max={r.maxMarks}
                      className="input max-w-[120px]"
                      placeholder={`0 – ${r.maxMarks}`}
                      value={grades[r.responseId] ?? ''}
                      onChange={(e) => setGrades((p) => ({ ...p, [r.responseId]: e.target.value }))}
                      disabled={saved[r.responseId]}
                    />
                    <button
                      className="btn-primary"
                      disabled={saving[r.responseId] || saved[r.responseId] || grades[r.responseId] === undefined}
                      onClick={() => handleGrade(r.responseId)}
                    >
                      {saving[r.responseId] ? 'Saving…' : saved[r.responseId] ? 'Saved' : 'Save grade'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  )
}
