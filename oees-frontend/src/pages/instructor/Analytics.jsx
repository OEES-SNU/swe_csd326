import { useEffect, useState } from 'react'
import api from '../../api/axios'

export default function Analytics() {
  const [courses, setCourses] = useState([])
  const [exams, setExams] = useState([])
  const [selectedCourse, setSelectedCourse] = useState('')
  const [selectedExam, setSelectedExam] = useState('')

  const [stats, setStats] = useState(null)
  const [topPerformers, setTopPerformers] = useState([])
  const [difficulty, setDifficulty] = useState([])
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
    setSelectedExam('')
    setStats(null)
    setTopPerformers([])
    setDifficulty([])
    api.get(`/exams/course/${selectedCourse}`)
      .then((r) => {
        setExams(r.data)
        if (r.data.length > 0) setSelectedExam(String(r.data[0].id))
      })
      .catch(() => setExams([]))
  }, [selectedCourse])

  useEffect(() => {
    if (!selectedExam) return
    setLoading(true)
    setError('')
    setStats(null)
    setTopPerformers([])
    setDifficulty([])

    Promise.all([
      api.get(`/results/analytics/exam/${selectedExam}`),
      api.get(`/results/analytics/exam/${selectedExam}/top-performers`),
      api.get(`/results/analytics/exam/${selectedExam}/question/difficulty`),
    ])
      .then(([statsRes, topRes, diffRes]) => {
        setStats(statsRes.data)
        setTopPerformers(topRes.data)
        setDifficulty(diffRes.data)
      })
      .catch(() => setError('Analytics not available yet. Generate results first.'))
      .finally(() => setLoading(false))
  }, [selectedExam])

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Analytics</h1>
          <p className="page-subtitle">Student performance insights</p>
        </div>
      </div>

      <div className="flex items-center gap-3 mb-6 flex-wrap">
        <label className="text-sm font-medium text-gray-700">Course:</label>
        <select
          className="select max-w-xs"
          value={selectedCourse}
          onChange={(e) => setSelectedCourse(e.target.value)}
        >
          {courses.map((c) => (
            <option key={c.id} value={c.id}>{c.courseCode} — {c.courseName}</option>
          ))}
        </select>

        <label className="text-sm font-medium text-gray-700">Exam:</label>
        <select
          className="select max-w-xs"
          value={selectedExam}
          onChange={(e) => setSelectedExam(e.target.value)}
        >
          {exams.map((e) => (
            <option key={e.id} value={e.id}>{e.title}</option>
          ))}
        </select>
      </div>

      {loading && <div className="text-sm text-gray-400 py-12 text-center">Loading analytics…</div>}

      {error && !loading && (
        <div className="card p-8 text-center">
          <p className="text-gray-400 text-sm">{error}</p>
        </div>
      )}

      {stats && !loading && (
        <>
          {/* Summary cards */}
          <div className="grid md:grid-cols-4 gap-4 mb-6">
            {[
              ['Attempts', stats.totalAttempts ?? 0],
              ['Average score', stats.averageScore != null ? stats.averageScore.toFixed(1) : '—'],
              ['Highest score', stats.highestScore ?? '—'],
              ['Pass rate', stats.passRate != null ? `${stats.passRate.toFixed(1)}%` : '—'],
            ].map(([label, value]) => (
              <div key={label} className="card p-5">
                <p className="text-sm text-gray-500">{label}</p>
                <h2 className="text-2xl font-bold text-gray-900">{value}</h2>
              </div>
            ))}
          </div>

          {/* Top performers */}
          <div className="card p-6 mb-6">
            <h2 className="text-base font-semibold text-gray-900 mb-4">Top 10 Performers</h2>
            {topPerformers.length === 0 ? (
              <p className="text-sm text-gray-400">No results generated yet.</p>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left border-b border-gray-100">
                    <th className="py-2 font-medium text-gray-500">Rank</th>
                    <th className="font-medium text-gray-500">Student</th>
                    <th className="font-medium text-gray-500">Score</th>
                    <th className="font-medium text-gray-500">Grade</th>
                  </tr>
                </thead>
                <tbody>
                  {topPerformers.map((s, i) => (
                    <tr key={i} className="border-b border-gray-50">
                      <td className="py-2 text-gray-400">#{s.rank ?? i + 1}</td>
                      <td className="font-medium text-gray-900">{s.studentName}</td>
                      <td className="text-gray-700">
                        {s.score}
                        <span className="text-gray-400 text-xs"> / {s.maxMarks}</span>
                      </td>
                      <td>
                        <span className={`inline-block px-2 py-0.5 rounded text-xs font-semibold ${
                          s.grade === 'A+' || s.grade === 'A' ? 'bg-green-100 text-green-700' :
                          s.grade === 'B' ? 'bg-blue-100 text-blue-700' :
                          s.grade === 'C' ? 'bg-yellow-100 text-yellow-700' :
                          s.grade === 'D' ? 'bg-orange-100 text-orange-700' :
                          'bg-red-100 text-red-700'
                        }`}>{s.grade}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {/* Question difficulty */}
          <div className="card p-6">
            <h2 className="text-base font-semibold text-gray-900 mb-4">Question Difficulty</h2>
            {difficulty.length === 0 ? (
              <p className="text-sm text-gray-400">No data available.</p>
            ) : (
              <div className="space-y-3">
                {difficulty
                  .slice()
                  .sort((a, b) => b.difficultyPercentage - a.difficultyPercentage)
                  .map((q, i) => (
                    <div key={i}>
                      <div className="flex justify-between text-sm mb-1">
                        <span className="text-gray-700 truncate max-w-[75%]">{q.content}</span>
                        <span className={`font-medium ${q.difficultyPercentage >= 60 ? 'text-red-600' : q.difficultyPercentage >= 30 ? 'text-yellow-600' : 'text-green-600'}`}>
                          {q.difficultyPercentage.toFixed(0)}% wrong
                        </span>
                      </div>
                      <div className="w-full bg-gray-100 rounded-full h-1.5">
                        <div
                          className={`h-1.5 rounded-full ${q.difficultyPercentage >= 60 ? 'bg-red-500' : q.difficultyPercentage >= 30 ? 'bg-yellow-500' : 'bg-green-500'}`}
                          style={{ width: `${q.difficultyPercentage}%` }}
                        />
                      </div>
                    </div>
                  ))}
              </div>
            )}
          </div>
        </>
      )}
    </>
  )
}
