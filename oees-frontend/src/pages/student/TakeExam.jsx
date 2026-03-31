import { useState, useEffect, useRef } from 'react'
import { useParams, useLocation, useNavigate } from 'react-router-dom'
import api from '../../api/axios'

function Timer({ deadline, onExpire }) {
  const [remaining, setRemaining] = useState(() => {
    const diff = new Date(deadline) - Date.now()
    return Math.max(0, Math.floor(diff / 1000))
  })

  useEffect(() => {
    if (remaining <= 0) { onExpire(); return }
    const t = setInterval(() => {
      setRemaining((r) => {
        if (r <= 1) { clearInterval(t); onExpire(); return 0 }
        return r - 1
      })
    }, 1000)
    return () => clearInterval(t)
  }, [])

  const h = Math.floor(remaining / 3600)
  const m = Math.floor((remaining % 3600) / 60)
  const s = remaining % 60
  const isLow = remaining < 300

  return (
    <span className={`font-mono text-sm font-semibold tabular-nums ${isLow ? 'text-red-600' : 'text-gray-700'}`}>
      {h > 0 && `${String(h).padStart(2, '0')}:`}
      {String(m).padStart(2, '0')}:{String(s).padStart(2, '0')}
    </span>
  )
}

export default function TakeExam() {
  const { examId } = useParams()
  const { state } = useLocation()
  const navigate = useNavigate()
  const attempt = state?.attempt
  const submitted = useRef(false)

  const [answers, setAnswers] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [current, setCurrent] = useState(0)
  const [result, setResult] = useState(null)

  useEffect(() => {
    if (!attempt) navigate('/student/exams', { replace: true })
  }, [attempt])

  if (!attempt) return null

  const questions = attempt.questions ?? []

  const setAnswer = (qId, value) => {
    setAnswers((prev) => ({ ...prev, [qId]: value }))
  }

  const handleSubmit = async (auto = false) => {
    if (submitted.current) return
    if (!auto && !confirm('Submit exam? You cannot make changes after submitting.')) return
    submitted.current = true
    setSubmitting(true)

    const responses = questions.map((q) => ({
      questionId: q.questionId,
      selectedOption: q.type === 'MULTIPLE_CHOICE' ? (answers[q.questionId] ?? null) : null,
      responseText: q.type !== 'MULTIPLE_CHOICE' ? (answers[q.questionId] ?? '') : null,
    }))

    try {
      const { data } = await api.post(`/exams/${examId}/attempt/${attempt.attemptId}/submit`, { responses })
      setResult(data)
    } catch (err) {
      alert(err.response?.data?.message ?? 'Submission failed. Please try again.')
      submitted.current = false
      setSubmitting(false)
    }
  }

  const q = questions[current]
  const answered = Object.keys(answers).length
  const isMCQ = q?.type === 'MULTIPLE_CHOICE'
  const isFill = q?.type === 'FILL_IN_THE_BLANK'

  if (result) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
        <div className="card p-10 max-w-sm w-full text-center">
          <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-5">
            <svg className="w-6 h-6 text-gray-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 className="text-xl font-semibold text-gray-900 mb-1">Exam submitted</h2>
          {result.pendingEvaluation ? (
            <>
              <p className="text-sm text-gray-500 mb-6">
                Your responses have been recorded. The instructor will evaluate descriptive answers and publish your final result.
              </p>
              <span className="inline-block px-3 py-1 text-xs font-medium bg-yellow-50 text-yellow-700 border border-yellow-200 rounded-full">
                Pending evaluation
              </span>
            </>
          ) : (
            <>
              <p className="text-sm text-gray-500 mb-4">Your score</p>
              <p className="text-5xl font-bold text-gray-900 mb-1">
                {result.score}
                <span className="text-2xl font-medium text-gray-400"> / {result.totalMarks}</span>
              </p>
              <p className="text-sm text-gray-400 mt-2">
                {Math.round((result.score / result.totalMarks) * 100)}%
              </p>
            </>
          )}
          <button
            className="btn-secondary w-full mt-8"
            onClick={() => navigate('/student/courses')}
          >
            Back to courses
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Top bar */}
      <header className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between sticky top-0 z-10">
        <div>
          <p className="text-sm font-semibold text-gray-900">Exam in progress</p>
          <p className="text-xs text-gray-400">Attempt #{attempt.attemptId}</p>
        </div>
        <div className="flex items-center gap-6">
          <div className="text-center">
            <p className="text-xs text-gray-400 mb-0.5">Time remaining</p>
            <Timer deadline={attempt.deadline} onExpire={() => handleSubmit(true)} />
          </div>
          <div className="text-center">
            <p className="text-xs text-gray-400 mb-0.5">Answered</p>
            <p className="text-sm font-semibold text-gray-700">{answered} / {questions.length}</p>
          </div>
          <button
            className="btn-primary"
            onClick={() => handleSubmit(false)}
            disabled={submitting}
          >
            {submitting ? 'Submitting…' : 'Submit exam'}
          </button>
        </div>
      </header>

      <div className="flex flex-1 max-w-5xl mx-auto w-full gap-6 p-6">
        {/* Question list sidebar */}
        <aside className="w-48 flex-shrink-0">
          <div className="card p-3 sticky top-24">
            <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 px-1">Questions</p>
            <div className="grid grid-cols-5 gap-1">
              {questions.map((q, i) => {
                const isAnswered = answers[q.questionId] !== undefined && answers[q.questionId] !== ''
                const isCurrent = i === current
                return (
                  <button
                    key={q.questionId}
                    onClick={() => setCurrent(i)}
                    className={`w-full aspect-square text-xs font-medium rounded transition-colors
                      ${isCurrent ? 'bg-gray-900 text-white' : isAnswered ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'}`}
                  >
                    {i + 1}
                  </button>
                )
              })}
            </div>
          </div>
        </aside>

        {/* Question panel */}
        <main className="flex-1 min-w-0">
          {q && (
            <div className="card p-6">
              <div className="flex items-start justify-between gap-4 mb-5">
                <div>
                  <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">
                    Question {current + 1} of {questions.length} · {q.marks} marks
                  </p>
                  <p className="text-base font-medium text-gray-900 leading-relaxed">{q.content}</p>
                </div>
                <span className={`badge-gray flex-shrink-0 ${q.type === 'MULTIPLE_CHOICE' ? 'badge-blue' : ''}`}>
                  {q.type === 'MULTIPLE_CHOICE' ? 'MCQ' : q.type === 'FILL_IN_THE_BLANK' ? 'Fill-in' : 'Descriptive'}
                </span>
              </div>

              {isMCQ && (
                <div className="space-y-2">
                  {(q.options ?? []).map((opt, oi) => {
                    const letter = ['A', 'B', 'C', 'D'][oi]
                    const selected = answers[q.questionId] === letter
                    return (
                      <label
                        key={letter}
                        className={`flex items-center gap-3 p-3 rounded border cursor-pointer transition-colors
                          ${selected ? 'border-gray-900 bg-gray-50' : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'}`}
                      >
                        <input
                          type="radio"
                          name={`q-${q.questionId}`}
                          value={letter}
                          checked={selected}
                          onChange={() => setAnswer(q.questionId, letter)}
                          className="text-gray-900"
                        />
                        <span className="text-xs font-semibold text-gray-500 w-4">{letter}</span>
                        <span className="text-sm text-gray-800">{opt}</span>
                      </label>
                    )
                  })}
                </div>
              )}

              {isFill && (
                <input
                  type="text"
                  className="input max-w-md"
                  placeholder="Your answer…"
                  value={answers[q.questionId] ?? ''}
                  onChange={(e) => setAnswer(q.questionId, e.target.value)}
                />
              )}

              {!isMCQ && !isFill && (
                <textarea
                  className="input resize-none w-full"
                  rows={6}
                  placeholder="Write your answer here…"
                  value={answers[q.questionId] ?? ''}
                  onChange={(e) => setAnswer(q.questionId, e.target.value)}
                />
              )}

              <div className="flex justify-between mt-6 pt-5 border-t border-gray-100">
                <button
                  className="btn-secondary"
                  onClick={() => setCurrent((p) => Math.max(0, p - 1))}
                  disabled={current === 0}
                >
                  Previous
                </button>
                <button
                  className="btn-primary"
                  onClick={() => setCurrent((p) => Math.min(questions.length - 1, p + 1))}
                  disabled={current === questions.length - 1}
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}
