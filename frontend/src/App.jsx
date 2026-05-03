import { useEffect, useState } from "react";
import api from "./api";
import { API_BASE } from "./config";

const emptyAuth = { token: "", role: "", name: "" };

export default function App() {
  const [auth, setAuth] = useState(() => ({
    token: localStorage.getItem("token") || "",
    role: localStorage.getItem("role") || "",
    name: localStorage.getItem("name") || ""
  }));
  const [courses, setCourses] = useState([]);
  const [myCourses, setMyCourses] = useState([]);
  const [selectedCourseId, setSelectedCourseId] = useState("");
  const [contents, setContents] = useState([]);
  const [assignments, setAssignments] = useState([]);
  const [submissions, setSubmissions] = useState([]);
  const [message, setMessage] = useState("");

  const [registerForm, setRegisterForm] = useState({ name: "", email: "", password: "", role: "STUDENT" });
  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [courseForm, setCourseForm] = useState({ title: "", description: "" });
  const [assignmentForm, setAssignmentForm] = useState({ title: "", description: "", dueDate: "" });

  useEffect(() => {
    if (auth.token) {
      loadCourses();
      loadMyCourses();
    }
  }, [auth.token]);

  const setSession = (res) => {
    localStorage.setItem("token", res.token);
    localStorage.setItem("role", res.role);
    localStorage.setItem("name", res.name);
    setAuth({ token: res.token, role: res.role, name: res.name });
  };

  const logout = () => {
    localStorage.clear();
    setAuth(emptyAuth);
    setCourses([]);
    setMyCourses([]);
    setSelectedCourseId("");
    setContents([]);
    setAssignments([]);
    setSubmissions([]);
  };

  const loadCourses = async () => {
    const res = await api.get("/courses");
    setCourses(res.data);
  };

  const loadMyCourses = async () => {
    const res = await api.get("/courses/my");
    setMyCourses(res.data);
  };

  const loadCourseDetail = async (courseId) => {
    setSelectedCourseId(courseId);
    const [contentRes, assignmentRes] = await Promise.all([
      api.get(`/courses/${courseId}/contents`),
      api.get(`/courses/${courseId}/assignments`)
    ]);
    setContents(contentRes.data);
    setAssignments(assignmentRes.data);
    setSubmissions([]);
  };

  const onRegister = async (e) => {
    e.preventDefault();
    const res = await api.post("/auth/register", registerForm);
    setSession(res.data);
  };

  const onLogin = async (e) => {
    e.preventDefault();
    const res = await api.post("/auth/login", loginForm);
    setSession(res.data);
  };

  const createCourse = async (e) => {
    e.preventDefault();
    await api.post("/courses", courseForm);
    setCourseForm({ title: "", description: "" });
    await loadCourses();
    await loadMyCourses();
  };

  const enroll = async (courseId) => {
    await api.post(`/courses/${courseId}/enroll`);
    await loadMyCourses();
  };

  const uploadContent = async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    await api.post(`/courses/${selectedCourseId}/contents`, formData, {
      headers: { "Content-Type": "multipart/form-data" }
    });
    e.target.reset();
    await loadCourseDetail(selectedCourseId);
  };

  const createAssignment = async (e) => {
    e.preventDefault();
    await api.post(`/courses/${selectedCourseId}/assignments`, assignmentForm);
    setAssignmentForm({ title: "", description: "", dueDate: "" });
    await loadCourseDetail(selectedCourseId);
  };

  const submitAssignment = async (e, assignmentId) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    await api.post(`/assignments/${assignmentId}/submit`, formData, {
      headers: { "Content-Type": "multipart/form-data" }
    });
    e.target.reset();
    setMessage("Assignment submitted");
  };

  const loadSubmissions = async (assignmentId) => {
    const res = await api.get(`/assignments/${assignmentId}/submissions`);
    setSubmissions(res.data);
  };

  const gradeSubmission = async (submissionId, grade, feedback) => {
    await api.put(`/submissions/${submissionId}/grade`, { grade, feedback });
    setMessage("Submission graded");
  };

  if (!auth.token) {
    return (
      <div className="container">
        <h1>Online Course Management System</h1>
        <div className="grid">
          <form onSubmit={onRegister} className="card">
            <h3>Register</h3>
            <input placeholder="Name" value={registerForm.name} onChange={(e) => setRegisterForm({ ...registerForm, name: e.target.value })} required />
            <input placeholder="Email" type="email" value={registerForm.email} onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })} required />
            <input placeholder="Password" type="password" value={registerForm.password} onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })} required />
            <select value={registerForm.role} onChange={(e) => setRegisterForm({ ...registerForm, role: e.target.value })}>
              <option value="STUDENT">Student</option>
              <option value="INSTRUCTOR">Instructor</option>
            </select>
            <button type="submit">Register</button>
          </form>
          <form onSubmit={onLogin} className="card">
            <h3>Login</h3>
            <input placeholder="Email" type="email" value={loginForm.email} onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })} required />
            <input placeholder="Password" type="password" value={loginForm.password} onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })} required />
            <button type="submit">Login</button>
          </form>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <h1>OCMS Dashboard</h1>
      <p>Welcome {auth.name} ({auth.role})</p>
      <button onClick={logout}>Logout</button>
      {message && <p>{message}</p>}

      {auth.role === "INSTRUCTOR" && (
        <form onSubmit={createCourse} className="card">
          <h3>Create Course</h3>
          <input placeholder="Title" value={courseForm.title} onChange={(e) => setCourseForm({ ...courseForm, title: e.target.value })} required />
          <textarea placeholder="Description" value={courseForm.description} onChange={(e) => setCourseForm({ ...courseForm, description: e.target.value })} />
          <button type="submit">Create</button>
        </form>
      )}

      <div className="grid">
        <div className="card">
          <h3>All Courses</h3>
          {courses.map((course) => (
            <div key={course.id} className="row">
              <span>{course.title}</span>
              <button onClick={() => loadCourseDetail(course.id)}>Open</button>
              {auth.role === "STUDENT" && <button onClick={() => enroll(course.id)}>Enroll</button>}
            </div>
          ))}
        </div>

        <div className="card">
          <h3>My Courses</h3>
          {myCourses.map((course) => <div key={course.id}>{course.title}</div>)}
        </div>
      </div>

      {selectedCourseId && (
        <>
          {auth.role === "INSTRUCTOR" && (
            <div className="grid">
              <form onSubmit={uploadContent} className="card">
                <h3>Upload Content</h3>
                <input name="title" placeholder="Content title" required />
                <input name="file" type="file" required />
                <button type="submit">Upload</button>
              </form>
              <form onSubmit={createAssignment} className="card">
                <h3>Create Assignment</h3>
                <input placeholder="Title" value={assignmentForm.title} onChange={(e) => setAssignmentForm({ ...assignmentForm, title: e.target.value })} required />
                <textarea placeholder="Description" value={assignmentForm.description} onChange={(e) => setAssignmentForm({ ...assignmentForm, description: e.target.value })} />
                <input type="datetime-local" value={assignmentForm.dueDate} onChange={(e) => setAssignmentForm({ ...assignmentForm, dueDate: e.target.value ? `${e.target.value}:00` : "" })} />
                <button type="submit">Create Assignment</button>
              </form>
            </div>
          )}

          <div className="card">
            <h3>Course Content</h3>
            {contents.map((c) => (
              <div key={c.id}>
                {c.title} - <a href={`${API_BASE}/files/${c.filePath}`} target="_blank" rel="noreferrer">Download</a>
              </div>
            ))}
          </div>

          <div className="card">
            <h3>Assignments</h3>
            {assignments.map((a) => (
              <div key={a.id} className="assignment">
                <strong>{a.title}</strong>
                <p>{a.description}</p>
                {auth.role === "STUDENT" && (
                  <form onSubmit={(e) => submitAssignment(e, a.id)}>
                    <input name="file" type="file" required />
                    <button type="submit">Submit</button>
                  </form>
                )}
                {auth.role === "INSTRUCTOR" && <button onClick={() => loadSubmissions(a.id)}>View Submissions</button>}
              </div>
            ))}
          </div>

          {auth.role === "INSTRUCTOR" && submissions.length > 0 && (
            <div className="card">
              <h3>Submissions</h3>
              {submissions.map((s) => (
                <SubmissionGrade key={s.id} submission={s} onGrade={gradeSubmission} />
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}

function SubmissionGrade({ submission, onGrade }) {
  const [grade, setGrade] = useState(submission.grade || "");
  const [feedback, setFeedback] = useState(submission.feedback || "");

  return (
    <div className="assignment">
      <div>
        {submission.student?.name || submission.student?.email} - {" "}
        <a href={`${API_BASE}/files/${submission.filePath}`} target="_blank" rel="noreferrer">Submission File</a>
      </div>
      <input type="number" placeholder="Grade" value={grade} onChange={(e) => setGrade(e.target.value)} />
      <input placeholder="Feedback" value={feedback} onChange={(e) => setFeedback(e.target.value)} />
      <button onClick={() => onGrade(submission.id, grade, feedback)}>Save Grade</button>
    </div>
  );
}
