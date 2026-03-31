import { useState, useEffect } from 'react'
import api from '../../api/axios'

export default function MyCourses() {
  const [courses, setCourses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/exams/my-courses')
      .then((r) => setCourses(r.data))
      .catch(() => setError('Failed to load courses.'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">My Courses</h1>
          <p className="page-subtitle">Courses you are enrolled in</p>
        </div>
      </div>

      {error && (
        <div className="mb-4 px-3 py-2.5 bg-red-50 border border-red-200 rounded text-sm text-red-700">{error}</div>
      )}

      {loading ? (
        <div className="text-sm text-gray-400 py-12 text-center">Loading…</div>
      ) : courses.length === 0 ? (
        <div className="card p-12 text-center">
          <p className="text-gray-400 text-sm">You are not enrolled in any courses yet.</p>
          <p className="text-gray-300 text-xs mt-1">Contact your administrator to get enrolled.</p>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {courses.map((c) => (
            <div key={c.id} className="card p-5">
              <div className="flex items-start justify-between gap-2 mb-2">
                <span className="text-xs font-mono font-semibold text-gray-400 bg-gray-100 px-2 py-0.5 rounded">
                  {c.courseCode}
                </span>
                <span className={`badge ${c.active ? 'badge-green' : 'badge-red'}`}>
                  {c.active ? 'Active' : 'Inactive'}
                </span>
              </div>
              <h2 className="text-sm font-semibold text-gray-900 mt-2">{c.courseName}</h2>
              {c.description && (
                <p className="text-xs text-gray-500 mt-1 line-clamp-2">{c.description}</p>
              )}
              {c.instructor && (
                <p className="text-xs text-gray-400 mt-3 pt-3 border-t border-gray-100">
                  Instructor: <span className="text-gray-600 font-medium">{c.instructor.name}</span>
                </p>
              )}
            </div>
          ))}
        </div>
      )}
    </>
  )
}
