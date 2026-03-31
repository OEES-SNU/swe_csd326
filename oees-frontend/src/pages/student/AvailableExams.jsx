import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../../api/axios'

const statusBadge = (s) => {
  const map = { DRAFT: 'badge-gray', SCHEDULED: 'badge-blue', ACTIVE: 'badge-green', EXPIRED: 'badge-red' }
  return <span className={map[s] ?? 'badge-gray'}>{s}</span>
}

const fmt = (dt) => dt ? new Date(dt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : '—'

export default function AvailableExams() {
  const [exams, setExams] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [starting, setStarting] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    api.get('/exams/available')
      .then((r) => setExams(r.data))
      .catch(() => setError('Failed to load exams.'))
      .finally(() => setLoading(false))
  }, [])

  const startExam = async (examId) => {
    setStarting(examId)
    try {
      const { data } = await api.post(`/exams/${examId}/attempt/start`)
      navigate(`/student/exam/${examId}`, { state: { attempt: data } })
    } catch (err) {
      alert(err.response?.data?.message ?? 'Could not start exam.')
    } finally {
      setStarting(null)
    }
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Available Exams</h1>
          <p className="page-subtitle">Exams you are eligible to take</p>
        </div>
      </div>

      {error && (
        <div className="mb-4 px-3 py-2.5 bg-red-50 border border-red-200 rounded text-sm text-red-700">{error}</div>
      )}

      {loading ? (
        <div className="text-sm text-gray-400 py-12 text-center">Loading…</div>
      ) : exams.length === 0 ? (
        <div className="card p-12 text-center">
          <p className="text-gray-400 text-sm">No exams available right now.</p>
          <p className="text-gray-300 text-xs mt-1">Check back later or contact your instructor.</p>
        </div>
      ) : (
        <div className="grid gap-4">
          {exams.map((ex) => (
            <div key={ex.id} className="card p-5 flex items-center gap-6">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <h2 className="text-base font-semibold text-gray-900">{ex.title}</h2>
                  {statusBadge(ex.status)}
                </div>
                <p className="text-sm text-gray-500">{ex.courseName}</p>
                <div className="flex items-center gap-4 mt-2 text-xs text-gray-400">
                  <span>{ex.durationMinutes} min</span>
                  <span>{ex.totalMarks} marks</span>
                  <span>Pass: {ex.passMark}</span>
                  <span>{ex.questionCount} questions</span>
                  <span>Max attempts: {ex.maxAttempts}</span>
                </div>
                <div className="flex items-center gap-4 mt-1 text-xs text-gray-400">
                  <span>Starts: {fmt(ex.startTime)}</span>
                  <span>Ends: {fmt(ex.endTime)}</span>
                </div>
              </div>
              <div className="flex-shrink-0">
                <button
                  className="btn-primary"
                  disabled={ex.status !== 'ACTIVE' || starting === ex.id}
                  onClick={() => startExam(ex.id)}
                >
                  {starting === ex.id ? 'Starting…' : 'Start exam'}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  )
}
