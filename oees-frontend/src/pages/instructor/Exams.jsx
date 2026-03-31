import { useState, useEffect } from 'react'
import api from '../../api/axios'
import Modal from '../../components/Modal'

const statusBadge = (s) => {
  const map = { DRAFT: 'badge-gray', SCHEDULED: 'badge-blue', ACTIVE: 'badge-green', EXPIRED: 'badge-red' }
  return <span className={map[s] ?? 'badge-gray'}>{s}</span>
}

const fmt = (dt) => dt ? new Date(dt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : '—'

const DETAILS_FORM = {
  title: '', durationMinutes: '', totalMarks: '',
  maxAttempts: '1', passMark: '', startTime: '', endTime: '',
}

const FILTERS_INIT = { unit: '', difficulty: '', type: '' }

export default function Exams() {
  const [courses, setCourses] = useState([])
  const [selectedCourse, setSelectedCourse] = useState('')
  const [exams, setExams] = useState([])
  const [allQuestions, setAllQuestions] = useState([])
  const [loading, setLoading] = useState(false)

  // wizard state
  const [wizardOpen, setWizardOpen] = useState(false)
  const [step, setStep] = useState(1) // 1 = pick questions, 2 = exam details
  const [filters, setFilters] = useState(FILTERS_INIT)
  const [selectedQIds, setSelectedQIds] = useState([])
  const [details, setDetails] = useState(DETAILS_FORM)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  // analytics
  const [analyticsOpen, setAnalyticsOpen] = useState(null)
  const [analytics, setAnalytics] = useState(null)

  const loadExams = (courseId) => {
    setLoading(true)
    api.get(`/exams/course/${courseId}`)
      .then((r) => setExams(r.data))
      .catch(() => setExams([]))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    api.get('/instructor/questions/my-courses').then((r) => {
      setCourses(r.data)
      if (r.data.length > 0) {
        setSelectedCourse(String(r.data[0].id))
        loadExams(r.data[0].id)
      }
    })
  }, [])

  useEffect(() => {
    if (!selectedCourse) return
    loadExams(selectedCourse)
    api.get(`/instructor/questions/course/${selectedCourse}`)
      .then((r) => setAllQuestions(r.data))
      .catch(() => setAllQuestions([]))
  }, [selectedCourse])

  const openWizard = () => {
    setStep(1)
    setFilters(FILTERS_INIT)
    setSelectedQIds([])
    setDetails(DETAILS_FORM)
    setFormError('')
    setWizardOpen(true)
  }

  const toggleQ = (id) => {
    setSelectedQIds((prev) =>
      prev.includes(id) ? prev.filter((q) => q !== id) : [...prev, id]
    )
  }

  const filteredQuestions = allQuestions.filter((q) => {
    if (filters.difficulty && q.difficultyLevel !== filters.difficulty) return false
    if (filters.type && q.type !== filters.type) return false
    if (filters.unit && !q.unit?.toLowerCase().includes(filters.unit.toLowerCase())) return false
    return true
  })

  const selectedQuestions = allQuestions.filter((q) => selectedQIds.includes(q.id))
  const totalSelectedMarks = selectedQuestions.reduce((sum, q) => sum + q.marks, 0)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      await api.post('/exams', {
        title: details.title,
        courseId: Number(selectedCourse),
        durationMinutes: Number(details.durationMinutes),
        totalMarks: Number(details.totalMarks),
        maxAttempts: Number(details.maxAttempts),
        passMark: Number(details.passMark),
        startTime: details.startTime || null,
        endTime: details.endTime || null,
        questionIds: selectedQIds,
        autoSelect: null,
      })
      setWizardOpen(false)
      loadExams(selectedCourse)
    } catch (err) {
      setFormError(err.response?.data?.message ?? 'Failed to create exam.')
    } finally {
      setSaving(false)
    }
  }

  const openAnalytics = async (exam) => {
    setAnalyticsOpen(exam)
    setAnalytics(null)
    try {
      const [aRes, avgRes] = await Promise.all([
        api.get(`/results/analytics/exam/${exam.id}`),
        api.get(`/results/analytics/course/${selectedCourse}/average`),
      ])
      setAnalytics({ exam: aRes.data, courseAvg: avgRes.data })
    } catch {
      setAnalytics({ error: 'Analytics not available yet.' })
    }
  }

  const generateResults = async (examId) => {
    try {
      await api.post(`/results/generate/${examId}`)
      alert('Results generated successfully.')
    } catch (err) {
      alert(err.response?.data?.message ?? 'Failed to generate results.')
    }
  }

  const units = [...new Set(allQuestions.map((q) => q.unit).filter(Boolean))]

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Exams</h1>
          <p className="page-subtitle">Create and manage exams for your courses</p>
        </div>
        <button className="btn-primary" onClick={openWizard}>Create exam</button>
      </div>

      <div className="flex items-center gap-3 mb-5">
        <label className="text-sm font-medium text-gray-700">Course:</label>
        <select className="select max-w-xs" value={selectedCourse}
          onChange={(e) => setSelectedCourse(e.target.value)}>
          {courses.map((c) => (
            <option key={c.id} value={c.id}>{c.courseCode} — {c.courseName}</option>
          ))}
        </select>
      </div>

      {loading ? (
        <div className="text-sm text-gray-400 py-12 text-center">Loading…</div>
      ) : (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Duration</th>
                <th>Marks</th>
                <th>Questions</th>
                <th>Start</th>
                <th>End</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {exams.length === 0 && (
                <tr>
                  <td colSpan={8} className="text-center text-gray-400 py-10">No exams yet for this course.</td>
                </tr>
              )}
              {exams.map((ex) => (
                <tr key={ex.id}>
                  <td className="font-medium text-gray-900">{ex.title}</td>
                  <td className="text-gray-500">{ex.durationMinutes} min</td>
                  <td>{ex.totalMarks} / pass {ex.passMark}</td>
                  <td>{ex.questionCount}</td>
                  <td className="text-gray-500 text-xs">{fmt(ex.startTime)}</td>
                  <td className="text-gray-500 text-xs">{fmt(ex.endTime)}</td>
                  <td>{statusBadge(ex.status)}</td>
                  <td className="text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button className="btn-secondary btn-sm" onClick={() => openAnalytics(ex)}>Analytics</button>
                      <button className="btn-secondary btn-sm" onClick={() => generateResults(ex.id)}>Generate results</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ── Exam Creation Wizard ── */}
      {wizardOpen && (
        <div className="fixed inset-0 z-50 flex flex-col bg-gray-50">
          {/* Wizard header */}
          <div className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between flex-shrink-0">
            <div className="flex items-center gap-4">
              <button onClick={() => setWizardOpen(false)} className="text-gray-400 hover:text-gray-700 transition-colors">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
              <h2 className="text-base font-semibold text-gray-900">Create Exam</h2>
            </div>

            {/* Steps indicator */}
            <div className="flex items-center gap-2 text-sm">
              <div className={`flex items-center gap-1.5 px-3 py-1 rounded-full font-medium ${step === 1 ? 'bg-gray-900 text-white' : 'bg-green-100 text-green-700'}`}>
                {step > 1 ? (
                  <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                  </svg>
                ) : <span className="w-3.5 h-3.5 flex items-center justify-center text-xs">1</span>}
                Select Questions
              </div>
              <div className="w-6 h-px bg-gray-300" />
              <div className={`flex items-center gap-1.5 px-3 py-1 rounded-full font-medium ${step === 2 ? 'bg-gray-900 text-white' : 'bg-gray-100 text-gray-400'}`}>
                <span className="w-3.5 h-3.5 flex items-center justify-center text-xs">2</span>
                Exam Details
              </div>
            </div>

            <div className="w-32" />
          </div>

          {/* Step 1 — Question selection */}
          {step === 1 && (
            <div className="flex flex-1 min-h-0">
              {/* Left: filters + question list */}
              <div className="flex-1 flex flex-col min-w-0 border-r border-gray-200">
                {/* Filters bar */}
                <div className="bg-white border-b border-gray-200 px-6 py-3 flex items-center gap-4 flex-shrink-0">
                  <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Filter</span>
                  <select className="select max-w-[140px] text-xs py-1.5"
                    value={filters.difficulty}
                    onChange={(e) => setFilters({ ...filters, difficulty: e.target.value })}>
                    <option value="">All difficulties</option>
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                  <select className="select max-w-[160px] text-xs py-1.5"
                    value={filters.type}
                    onChange={(e) => setFilters({ ...filters, type: e.target.value })}>
                    <option value="">All types</option>
                    <option value="MULTIPLE_CHOICE">MCQ</option>
                    <option value="FILL_IN_THE_BLANK">Fill in the Blank</option>
                    <option value="DESCRIPTIVE">Descriptive</option>
                  </select>
                  <select className="select max-w-[140px] text-xs py-1.5"
                    value={filters.unit}
                    onChange={(e) => setFilters({ ...filters, unit: e.target.value })}>
                    <option value="">All units</option>
                    {units.map((u) => <option key={u} value={u}>{u}</option>)}
                  </select>
                  <button className="text-xs text-gray-400 hover:text-gray-700 underline"
                    onClick={() => setFilters(FILTERS_INIT)}>
                    Clear
                  </button>
                  <span className="ml-auto text-xs text-gray-400">{filteredQuestions.length} questions</span>
                </div>

                {/* Question list */}
                <div className="flex-1 overflow-y-auto p-6">
                  {filteredQuestions.length === 0 ? (
                    <div className="text-center text-gray-400 text-sm py-16">
                      No questions match the current filters.
                    </div>
                  ) : (
                    <div className="space-y-2">
                      {filteredQuestions.map((q) => {
                        const selected = selectedQIds.includes(q.id)
                        return (
                          <label key={q.id}
                            className={`flex items-start gap-4 p-4 rounded-lg border cursor-pointer transition-colors ${selected ? 'border-gray-900 bg-white' : 'border-gray-200 bg-white hover:border-gray-300'}`}>
                            <input type="checkbox" className="mt-0.5 flex-shrink-0"
                              checked={selected} onChange={() => toggleQ(q.id)} />
                            <div className="flex-1 min-w-0">
                              <p className="text-sm text-gray-900 leading-snug">{q.content}</p>
                              <div className="flex items-center gap-2 mt-1.5">
                                <span className="text-xs text-gray-400">
                                  {q.type === 'MULTIPLE_CHOICE' ? 'MCQ' : q.type === 'FILL_IN_THE_BLANK' ? 'Fill-in' : 'Descriptive'}
                                </span>
                                <span className="text-gray-200">·</span>
                                <span className={`text-xs font-medium ${q.difficultyLevel === 'EASY' ? 'text-green-600' : q.difficultyLevel === 'MEDIUM' ? 'text-yellow-600' : 'text-red-600'}`}>
                                  {q.difficultyLevel}
                                </span>
                                {q.unit && <><span className="text-gray-200">·</span><span className="text-xs text-gray-400">{q.unit}</span></>}
                                <span className="text-gray-200">·</span>
                                <span className="text-xs font-medium text-gray-600">{q.marks} marks</span>
                              </div>
                            </div>
                          </label>
                        )
                      })}
                    </div>
                  )}
                </div>
              </div>

              {/* Right: selected summary */}
              <div className="w-72 flex-shrink-0 flex flex-col bg-white">
                <div className="px-5 py-4 border-b border-gray-200">
                  <p className="text-sm font-semibold text-gray-900">Selected Questions</p>
                  <p className="text-xs text-gray-400 mt-0.5">{selectedQIds.length} selected · {totalSelectedMarks} total marks</p>
                </div>
                <div className="flex-1 overflow-y-auto p-4 space-y-2">
                  {selectedQIds.length === 0 && (
                    <p className="text-xs text-gray-400 text-center py-8">No questions selected yet.</p>
                  )}
                  {selectedQuestions.map((q, i) => (
                    <div key={q.id} className="flex items-start gap-2 p-2.5 bg-gray-50 rounded border border-gray-100">
                      <span className="text-xs text-gray-400 font-medium w-5 flex-shrink-0 mt-0.5">{i + 1}.</span>
                      <div className="flex-1 min-w-0">
                        <p className="text-xs text-gray-800 line-clamp-2">{q.content}</p>
                        <p className="text-xs text-gray-400 mt-0.5">{q.marks} marks</p>
                      </div>
                      <button onClick={() => toggleQ(q.id)} className="text-gray-300 hover:text-red-500 flex-shrink-0">
                        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                    </div>
                  ))}
                </div>
                <div className="p-4 border-t border-gray-200">
                  <button
                    className="btn-primary w-full"
                    disabled={selectedQIds.length === 0}
                    onClick={() => { setFormError(''); setStep(2) }}
                  >
                    Continue with {selectedQIds.length} question{selectedQIds.length !== 1 ? 's' : ''} →
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Step 2 — Exam details */}
          {step === 2 && (
            <div className="flex-1 overflow-y-auto flex items-start justify-center p-8">
              <div className="w-full max-w-xl">
                <div className="card p-6">
                  <div className="flex items-center justify-between mb-5">
                    <div>
                      <h3 className="text-base font-semibold text-gray-900">Exam Details</h3>
                      <p className="text-xs text-gray-400 mt-0.5">{selectedQIds.length} questions · {totalSelectedMarks} total marks available</p>
                    </div>
                    <button className="text-sm text-gray-400 hover:text-gray-700 underline" onClick={() => setStep(1)}>
                      ← Back
                    </button>
                  </div>

                  {formError && (
                    <div className="mb-4 px-3 py-2.5 bg-red-50 border border-red-200 rounded text-sm text-red-700">
                      {formError}
                    </div>
                  )}

                  <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="form-group">
                      <label className="label">Exam title</label>
                      <input className="input" required placeholder="e.g. Midterm Exam 2026"
                        value={details.title}
                        onChange={(e) => setDetails({ ...details, title: e.target.value })} />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div className="form-group mb-0">
                        <label className="label">Duration (minutes)</label>
                        <input type="number" min="1" className="input" required
                          value={details.durationMinutes}
                          onChange={(e) => setDetails({ ...details, durationMinutes: e.target.value })} />
                      </div>
                      <div className="form-group mb-0">
                        <label className="label">Max attempts</label>
                        <input type="number" min="1" className="input" required
                          value={details.maxAttempts}
                          onChange={(e) => setDetails({ ...details, maxAttempts: e.target.value })} />
                      </div>
                      <div className="form-group mb-0">
                        <label className="label">Total marks</label>
                        <input type="number" min="1" className="input" required
                          placeholder={`Max: ${totalSelectedMarks}`}
                          value={details.totalMarks}
                          onChange={(e) => setDetails({ ...details, totalMarks: e.target.value })} />
                      </div>
                      <div className="form-group mb-0">
                        <label className="label">Pass mark</label>
                        <input type="number" min="0" className="input" required
                          value={details.passMark}
                          onChange={(e) => setDetails({ ...details, passMark: e.target.value })} />
                      </div>
                      <div className="form-group mb-0">
                        <label className="label">Start time</label>
                        <input type="datetime-local" className="input"
                          value={details.startTime}
                          onChange={(e) => setDetails({ ...details, startTime: e.target.value })} />
                      </div>
                      <div className="form-group mb-0">
                        <label className="label">End time</label>
                        <input type="datetime-local" className="input"
                          value={details.endTime}
                          onChange={(e) => setDetails({ ...details, endTime: e.target.value })} />
                      </div>
                    </div>

                    {/* Selected questions summary */}
                    <div className="border border-gray-200 rounded-lg overflow-hidden mt-2">
                      <div className="bg-gray-50 px-4 py-2 border-b border-gray-200">
                        <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider">
                          Questions included ({selectedQIds.length})
                        </p>
                      </div>
                      <div className="divide-y divide-gray-100 max-h-48 overflow-y-auto">
                        {selectedQuestions.map((q, i) => (
                          <div key={q.id} className="flex items-start gap-3 px-4 py-2.5">
                            <span className="text-xs text-gray-400 w-5 flex-shrink-0 mt-0.5">{i + 1}.</span>
                            <p className="text-xs text-gray-700 flex-1 line-clamp-1">{q.content}</p>
                            <span className="text-xs text-gray-400 flex-shrink-0">{q.marks} marks</span>
                          </div>
                        ))}
                      </div>
                    </div>

                    <div className="flex justify-end gap-2 pt-2">
                      <button type="button" className="btn-secondary" onClick={() => setWizardOpen(false)}>Cancel</button>
                      <button type="submit" className="btn-primary" disabled={saving}>
                        {saving ? 'Creating…' : 'Create exam'}
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Analytics Modal */}
      <Modal open={!!analyticsOpen} onClose={() => setAnalyticsOpen(null)} title={`Analytics — ${analyticsOpen?.title}`}>
        {!analytics && <p className="text-sm text-gray-400">Loading analytics…</p>}
        {analytics?.error && <p className="text-sm text-red-600">{analytics.error}</p>}
        {analytics && !analytics.error && (
          <div className="grid grid-cols-2 gap-4">
            {[
              ['Average score', analytics.exam?.averageScore?.toFixed(1) ?? '—'],
              ['Pass rate', analytics.exam?.passRate != null ? `${analytics.exam.passRate.toFixed(1)}%` : '—'],
              ['Highest score', analytics.exam?.highestScore ?? '—'],
              ['Lowest score', analytics.exam?.lowestScore ?? '—'],
              ['Total attempts', analytics.exam?.totalAttempts ?? '—'],
              ['Course average', typeof analytics.courseAvg === 'number' ? analytics.courseAvg.toFixed(1) : '—'],
            ].map(([k, v]) => (
              <div key={k} className="bg-gray-50 rounded p-3 border border-gray-100">
                <p className="text-xs text-gray-500 mb-0.5">{k}</p>
                <p className="text-lg font-semibold text-gray-900">{v}</p>
              </div>
            ))}
          </div>
        )}
      </Modal>
    </>
  )
}
