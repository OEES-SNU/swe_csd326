import { useState, useEffect } from 'react'
import api from '../../api/axios'

const fmt = (dt) =>
    dt ? new Date(dt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : '—'

const fmtServer = (dt) =>
    dt ? new Date(dt + 'Z').toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : '—'

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
      const data = Array.isArray(r.data) ? r.data : []
      setCourses(data)

      if (data.length > 0) {
        setSelectedCourse(String(data[0].id))
      }
    })
  }, [])

  useEffect(() => {
    if (!selectedCourse) return

    setSelectedExam(null)
    setAttempts([])
    setSelectedAttempt(null)
    setResponses([])
    setError('')

    api
        .get(`/exams/course/${selectedCourse}`)
        .then((r) => {
          setExams(Array.isArray(r.data) ? r.data : [])
        })
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
      setAttempts(Array.isArray(data) ? data : [])
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
      const { data } = await api.get(
          `/evaluation/attempt/${attempt.id}/pending`
      )

      const safeData = Array.isArray(data) ? data : []
      setResponses(safeData)

      if (safeData.length === 0) {
        setError('No pending responses for this attempt.')
      }
    } catch {
      setResponses([])
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
        {/* Header */}
        <div className="page-header mb-8">
          <div>
            <h1 className="page-title">Evaluation</h1>
            <p className="page-subtitle">
              Grade descriptive and fill-in-the-blank responses
            </p>
          </div>
        </div>

        {/* Course Selector */}
        <div className="card p-5 mb-6">
          <div className="flex flex-wrap items-center gap-3">
            <label className="text-sm font-medium text-gray-700">
              Course:
            </label>

            <select
                className="select min-w-[320px]"
                value={selectedCourse}
                onChange={(e) => setSelectedCourse(e.target.value)}
            >
              {courses.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.courseCode} — {c.courseName}
                  </option>
              ))}
            </select>

            {selectedExam && (
                <div className="ml-auto text-sm text-gray-500">
                  Selected Exam:{' '}
                  <span className="font-medium text-gray-800">
                {selectedExam.title}
              </span>
                </div>
            )}
          </div>
        </div>

        {/* Error */}
        {error && !loading && (
            <div className="px-4 py-3 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700 mb-6">
              {error}
            </div>
        )}

        {/* Main Layout */}
        <div className="grid grid-cols-12 gap-6">

          {/* LEFT SIDE */}
          <div className="col-span-4 space-y-6">

            {/* Exams */}
            <div>
              <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-3">
                Exams
              </p>

              <div className="card divide-y divide-gray-100 max-h-[360px] overflow-y-auto">
                {exams.length === 0 && (
                    <p className="text-sm text-gray-400 p-4">
                      No exams found.
                    </p>
                )}

                {exams.map((ex) => (
                    <button
                        key={ex.id}
                        onClick={() => loadAttempts(ex)}
                        className={`w-full text-left px-4 py-4 hover:bg-gray-50 transition ${
                            selectedExam?.id === ex.id
                                ? 'bg-gray-50 border-l-4 border-gray-900'
                                : ''
                        }`}
                    >
                      <p className="font-medium text-gray-900">
                        {ex.title}
                      </p>

                      <p className="text-xs text-gray-400 mt-1">
                        {ex.questionCount} questions · {ex.totalMarks} marks
                      </p>
                    </button>
                ))}
              </div>
            </div>

            {/* Attempts */}
            <div>
              <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-3">
                Submitted Attempts
              </p>

              <div className="card divide-y divide-gray-100 max-h-[360px] overflow-y-auto">
                {!selectedExam && (
                    <p className="text-sm text-gray-400 p-4">
                      Select an exam.
                    </p>
                )}

                {selectedExam && loading && !selectedAttempt && (
                    <p className="text-sm text-gray-400 p-4">
                      Loading...
                    </p>
                )}

                {selectedExam &&
                    !loading &&
                    attempts.length === 0 && (
                        <p className="text-sm text-gray-400 p-4">
                          No submitted attempts yet.
                        </p>
                    )}

                {attempts.map((a) => (
                    <button
                        key={a.id}
                        onClick={() => loadResponses(a)}
                        className={`w-full text-left px-4 py-4 hover:bg-gray-50 transition ${
                            selectedAttempt?.id === a.id
                                ? 'bg-gray-50 border-l-4 border-gray-900'
                                : ''
                        }`}
                    >
                      <p className="font-medium text-gray-900">
                        {a.studentName ??
                            a.student?.name ??
                            `Student #${a.id}`}
                      </p>

                      <p className="text-xs text-gray-400 mt-1">
                        Attempt #{a.attemptNumber} · {fmtServer(a.submittedAt)}
                      </p>
                    </button>
                ))}
              </div>
            </div>
          </div>

          {/* RIGHT SIDE */}
          <div className="col-span-8">

            <div className="flex items-center justify-between mb-3">
              <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider">
                Responses
              </p>

              {selectedAttempt && (
                  <p className="text-sm text-gray-400">
                    {selectedAttempt.studentName ??
                        selectedAttempt.student?.name ??
                        'Student'}{' '}
                    · Attempt #{selectedAttempt.attemptNumber}
                  </p>
              )}
            </div>

            {!selectedAttempt && (
                <div className="card p-12 text-center text-gray-400">
                  Select an attempt to review responses.
                </div>
            )}

            {selectedAttempt && loading && (
                <div className="card p-12 text-center text-gray-400">
                  Loading...
                </div>
            )}

            {!loading &&
                Array.isArray(responses) &&
                responses.length > 0 && (
                    <div className="space-y-5">
                      {responses.map((r, index) => (
                          <div
                              key={r.responseId}
                              className="card p-6"
                          >
                            <div className="flex items-start justify-between gap-4 mb-4">
                              <div>
                                <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">
                                  Question {index + 1} ·{' '}
                                  {r.questionType?.replace(/_/g, ' ')} ·{' '}
                                  {r.maxMarks} marks
                                </p>

                                <p className="font-medium text-gray-900">
                                  {r.questionContent}
                                </p>
                              </div>

                              {saved[r.responseId] && (
                                  <span className="badge-green">
                          Graded
                        </span>
                              )}
                            </div>

                            <div className="bg-gray-50 border border-gray-100 rounded-xl p-4 mb-5">
                              <p className="text-xs text-gray-400 mb-2">
                                Student Response
                              </p>

                              <p className="text-sm text-gray-700 whitespace-pre-wrap">
                                {r.responseText || (
                                    <em className="text-gray-400">
                                      No response
                                    </em>
                                )}
                              </p>
                            </div>

                            <div className="flex items-center gap-3">
                              <input
                                  type="number"
                                  min="0"
                                  max={r.maxMarks}
                                  className="input w-28"
                                  placeholder={`0-${r.maxMarks}`}
                                  value={grades[r.responseId] ?? ''}
                                  onChange={(e) =>
                                      setGrades((p) => ({
                                        ...p,
                                        [r.responseId]:
                                        e.target.value,
                                      }))
                                  }
                                  disabled={saved[r.responseId]}
                              />

                              <button
                                  className="btn-primary"
                                  disabled={
                                      saving[r.responseId] ||
                                      saved[r.responseId] ||
                                      grades[r.responseId] ===
                                      undefined
                                  }
                                  onClick={() =>
                                      handleGrade(r.responseId)
                                  }
                              >
                                {saving[r.responseId]
                                    ? 'Saving...'
                                    : saved[r.responseId]
                                        ? 'Saved'
                                        : 'Save Grade'}
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