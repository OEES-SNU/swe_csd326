import { useState, useEffect, useRef, useCallback } from 'react'
import { useParams, useLocation, useNavigate } from 'react-router-dom'
import api from '../../api/axios'

function Timer({ durationMinutes, onExpire }) {
  const secs = durationMinutes > 0 ? durationMinutes * 60 : 0
  const [remaining, setRemaining] = useState(secs)
  const onExpireRef = useRef(onExpire)
  onExpireRef.current = onExpire

  useEffect(() => {
    if (!durationMinutes || durationMinutes <= 0) return

    const timer = setInterval(() => {
      setRemaining((prev) => {
        if (prev <= 1) {
          clearInterval(timer)
          onExpireRef.current()
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => clearInterval(timer)
  }, [])

  const h = Math.floor(remaining / 3600)
  const m = Math.floor((remaining % 3600) / 60)
  const s = remaining % 60
  const low = remaining <= 300

  return (
      <span
          className={`font-mono text-sm font-semibold tabular-nums ${
              low ? 'text-red-600' : 'text-gray-700'
          }`}
      >
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
  const handleSubmitRef = useRef(null)

  const [answers, setAnswers] = useState({})
  const [current, setCurrent] = useState(0)
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState(null)

  // Anti-cheat
  const [violations, setViolations] = useState(0)
  const [warning, setWarning] = useState('')
  const [anticheatReady, setAnticheatReady] = useState(false)
  const MAX_VIOLATIONS = 3

  useEffect(() => {
    const t = setTimeout(() => setAnticheatReady(true), 3000)
    return () => clearTimeout(t)
  }, [])

  useEffect(() => {
    if (!attempt) {
      navigate('/student/exams', { replace: true })
    }
  }, [attempt, navigate])

  const questions = attempt?.questions ?? []

  const setAnswer = (questionId, value) => {
    setAnswers((prev) => ({
      ...prev,
      [questionId]: value,
    }))
  }

  const showWarning = (msg) => {
    setWarning(msg)
    setTimeout(() => setWarning(''), 2500)
  }

  const handleSubmit = useCallback(
      async (auto = false) => {
        if (submitted.current) return

        if (!auto) {
          const ok = window.confirm(
              'Submit exam? You cannot make changes after submitting.'
          )
          if (!ok) return
        }

        submitted.current = true
        setSubmitting(true)

        const responses = questions.map((q) => ({
          questionId: q.questionId,
          selectedOption:
              q.type === 'MULTIPLE_CHOICE'
                  ? answers[q.questionId] ?? null
                  : null,
          responseText:
              q.type !== 'MULTIPLE_CHOICE'
                  ? answers[q.questionId] ?? ''
                  : null,
        }))

        try {
          const { data } = await api.post(
              `/exams/${examId}/attempt/${attempt.attemptId}/submit`,
              { responses }
          )

          setResult(data)
        } catch (err) {
          alert(
              err.response?.data?.message ??
              'Submission failed. Please try again.'
          )
          submitted.current = false
          setSubmitting(false)
        }
      },
      [answers, questions, examId, attempt]
  )

  handleSubmitRef.current = handleSubmit

  useEffect(() => {
    if (violations === MAX_VIOLATIONS) {
      const t = setTimeout(() => handleSubmitRef.current(true), 1500)
      return () => clearTimeout(t)
    }
  }, [violations])

  // ======================
  // Stable Anti-Cheat
  // ======================
  useEffect(() => {
    if (!attempt) return

    const addViolation = (reason) => {
      if (!anticheatReady) return
      setViolations((prev) => {
        const next = prev + 1
        showWarning(next >= MAX_VIOLATIONS
          ? 'Maximum warnings reached. Auto submitting...'
          : `${reason} (${next}/${MAX_VIOLATIONS})`)
        return next
      })
    }

    const prevent = (e) => e.preventDefault()

    const onVisibility = () => {
      if (document.hidden) {
        addViolation('Tab switch detected')
      }
    }

    const onKeyDown = (e) => {
      const key = e.key.toLowerCase()

      if ((e.ctrlKey || e.metaKey) && ['c', 'v', 'x', 'a'].includes(key)) {
        e.preventDefault()
        showWarning('Copy / Paste blocked')
      }

      if (e.key === 'F12') {
        e.preventDefault()
        showWarning('Blocked')
      }

      if (
          e.ctrlKey &&
          e.shiftKey &&
          ['i', 'j', 'c'].includes(key)
      ) {
        e.preventDefault()
        showWarning('Blocked')
      }

      if (e.key === 'F5') {
        e.preventDefault()
      }
    }

    document.addEventListener('copy', prevent)
    document.addEventListener('cut', prevent)
    document.addEventListener('paste', prevent)
    document.addEventListener('contextmenu', prevent)

    document.addEventListener('visibilitychange', onVisibility)
    window.addEventListener('keydown', onKeyDown)

    return () => {
      document.removeEventListener('copy', prevent)
      document.removeEventListener('cut', prevent)
      document.removeEventListener('paste', prevent)
      document.removeEventListener('contextmenu', prevent)

      document.removeEventListener('visibilitychange', onVisibility)
      window.removeEventListener('keydown', onKeyDown)
    }
  }, [attempt, handleSubmit, anticheatReady])

  if (!attempt) return null

  const q = questions[current]
  const answered = Object.keys(answers).filter(
      (k) => answers[k] !== ''
  ).length

  const isMCQ = q?.type === 'MULTIPLE_CHOICE'
  const isFill = q?.type === 'FILL_IN_THE_BLANK'

  // ======================
  // Result Screen
  // ======================
  if (result) {
    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
          <div className="card p-10 max-w-sm w-full text-center">
            <h2 className="text-xl font-semibold text-gray-900 mb-3">
              Exam submitted
            </h2>

            {result.pendingEvaluation ? (
                <>
                  <p className="text-sm text-gray-500 mb-4">
                    Responses saved. Await instructor evaluation.
                  </p>

                  <span className="inline-block px-3 py-1 text-xs font-medium bg-yellow-50 text-yellow-700 border border-yellow-200 rounded-full">
                Pending evaluation
              </span>
                </>
            ) : (
                <>
                  <p className="text-sm text-gray-500 mb-2">
                    Your score
                  </p>

                  <p className="text-5xl font-bold text-gray-900">
                    {result.score}
                    <span className="text-2xl text-gray-400">
                  {' '}
                      / {result.totalMarks}
                </span>
                  </p>

                  <p className="text-sm text-gray-400 mt-2">
                    {Math.round(
                        (result.score / result.totalMarks) * 100
                    )}
                    %
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

  // ======================
  // Main Exam Screen
  // ======================
  return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        {/* Warning Toast */}
        {warning && (
            <div className="fixed top-4 right-4 z-50 bg-red-600 text-white px-4 py-2 rounded-lg shadow-lg text-sm">
              {warning}
            </div>
        )}

        {/* Header */}
        <header className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between sticky top-0 z-10">
          <div>
            <p className="text-sm font-semibold text-gray-900">
              Exam in progress
            </p>
            <p className="text-xs text-gray-400">
              Attempt #{attempt.attemptId}
            </p>
          </div>

          <div className="flex items-center gap-6">
            <div className="text-center">
              <p className="text-xs text-gray-400">Time remaining</p>
              <Timer
                  durationMinutes={attempt.durationMinutes}
                  onExpire={() => handleSubmit(true)}
              />
            </div>

            <div className="text-center">
              <p className="text-xs text-gray-400">Answered</p>
              <p className="text-sm font-semibold text-gray-700">
                {answered} / {questions.length}
              </p>
            </div>

            <div className="text-center">
              <p className="text-xs text-gray-400">Warnings</p>
              <p className="text-sm font-semibold text-red-600">
                {violations} / {MAX_VIOLATIONS}
              </p>
            </div>

            <button
                className="btn-primary"
                disabled={submitting}
                onClick={() => handleSubmit(false)}
            >
              {submitting ? 'Submitting…' : 'Submit exam'}
            </button>
          </div>
        </header>

        <div className="flex flex-1 max-w-6xl mx-auto w-full gap-6 p-6">
          {/* Sidebar */}
          <aside className="w-52 flex-shrink-0">
            <div className="card p-3 sticky top-24">
              <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-3">
                Questions
              </p>

              <div className="grid grid-cols-5 gap-2">
                {questions.map((item, i) => {
                  const done =
                      answers[item.questionId] !== undefined &&
                      answers[item.questionId] !== ''

                  const active = current === i

                  return (
                      <button
                          key={item.questionId}
                          onClick={() => setCurrent(i)}
                          className={`aspect-square rounded text-xs font-semibold
                    ${
                              active
                                  ? 'bg-gray-900 text-white'
                                  : done
                                      ? 'bg-green-100 text-green-700'
                                      : 'bg-gray-100 text-gray-500'
                          }`}
                      >
                        {i + 1}
                      </button>
                  )
                })}
              </div>
            </div>
          </aside>

          {/* Question Panel */}
          <main className="flex-1">
            {q && (
                <div className="card p-6">
                  <div className="flex items-start justify-between mb-5">
                    <div>
                      <p className="text-xs text-gray-400 uppercase mb-1">
                        Question {current + 1} of {questions.length} ·{' '}
                        {q.marks} marks
                      </p>

                      <p className="text-base font-medium text-gray-900">
                        {q.content}
                      </p>
                    </div>

                    <span className="badge-gray">
                  {isMCQ
                      ? 'MCQ'
                      : isFill
                          ? 'Fill-in'
                          : 'Descriptive'}
                </span>
                  </div>

                  {/* MCQ */}
                  {isMCQ && (
                      <div className="space-y-2">
                        {(q.options ?? []).map((opt, index) => {
                          const letter = ['A', 'B', 'C', 'D'][index]
                          const selected =
                              answers[q.questionId] === letter

                          return (
                              <label
                                  key={letter}
                                  className={`flex items-center gap-3 p-3 rounded border cursor-pointer ${
                                      selected
                                          ? 'border-gray-900 bg-gray-50'
                                          : 'border-gray-200'
                                  }`}
                              >
                                <input
                                    type="radio"
                                    checked={selected}
                                    onChange={() =>
                                        setAnswer(q.questionId, letter)
                                    }
                                />

                                <span className="w-4 text-xs font-semibold text-gray-500">
                          {letter}
                        </span>

                                <span>{opt}</span>
                              </label>
                          )
                        })}
                      </div>
                  )}

                  {/* Fill Blank */}
                  {isFill && (
                      <input
                          className="input max-w-md"
                          value={answers[q.questionId] ?? ''}
                          placeholder="Your answer..."
                          onChange={(e) =>
                              setAnswer(q.questionId, e.target.value)
                          }
                      />
                  )}

                  {/* Descriptive */}
                  {!isMCQ && !isFill && (
                      <textarea
                          rows={6}
                          className="input resize-none w-full"
                          placeholder="Write your answer..."
                          value={answers[q.questionId] ?? ''}
                          onChange={(e) =>
                              setAnswer(q.questionId, e.target.value)
                          }
                      />
                  )}

                  {/* Navigation */}
                  <div className="flex justify-between mt-6 pt-5 border-t border-gray-100">
                    <button
                        className="btn-secondary"
                        disabled={current === 0}
                        onClick={() =>
                            setCurrent((p) => Math.max(0, p - 1))
                        }
                    >
                      Previous
                    </button>

                    <button
                        className="btn-primary"
                        disabled={current === questions.length - 1}
                        onClick={() =>
                            setCurrent((p) =>
                                Math.min(questions.length - 1, p + 1)
                            )
                        }
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