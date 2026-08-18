import React, { useEffect, useState, useCallback } from 'react';
import Sidebar from '../components/Sidebar';
import SkillChips from '../components/SkillChips';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { useAuth } from '../context/AuthContext';
import api, { getErrorMessage, fileUrl } from '../api/axios';
import { ChatPanel } from './AlumniDashboard';

const NAV_ITEMS = [
  { key: 'profile', label: 'My Profile' },
  { key: 'directory', label: 'Alumni Directory' },
  { key: 'jobs', label: 'Job Portal' },
  { key: 'applications', label: 'My Applications' },
  { key: 'mentorship', label: 'Mentorship' },
  { key: 'messages', label: 'Messages' },
  { key: 'events', label: 'Events & Materials' },
];

export default function StudentDashboard() {
  const [tab, setTab] = useState('profile');
  return (
    <div className="dashboard">
      <Sidebar items={NAV_ITEMS} active={tab} onSelect={setTab} roleLabel="Student" />
      <main className="main-content">
        <div className="tab-content">
          {tab === 'profile' && <ProfileTab />}
          {tab === 'directory' && <DirectoryTab />}
          {tab === 'jobs' && <JobsTab />}
          {tab === 'applications' && <ApplicationsTab />}
          {tab === 'mentorship' && <MentorshipTab />}
          {tab === 'messages' && <MessagesTab />}
          {tab === 'events' && <EventsTab />}
        </div>
      </main>
    </div>
  );
}

/* ================= PROFILE TAB ================= */
function ProfileTab() {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({});
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);

  const load = useCallback(async () => {
    try {
      const { data } = await api.get('/student/profile');
      setProfile(data);
      setForm(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const { data } = await api.put('/student/profile', {
        branch: form.branch,
        graduationYear: form.graduationYear ? Number(form.graduationYear) : null,
        skills: form.skills,
        bio: form.bio,
      });
      setProfile(data);
      setForm(data);
      setSuccess('Profile updated successfully.');
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const handleResumeUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setUploading(true);
    setError('');
    try {
      const fd = new FormData();
      fd.append('file', file);
      const { data } = await api.post('/student/profile/resume', fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setProfile(data);
      setForm(data);
      setSuccess('Resume uploaded and skills auto-filled.');
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setUploading(false);
    }
  };

  if (!profile) return <div className="empty-state">Loading profile...</div>;

  return (
    <>
      <div className="topbar"><h2>My Profile</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="card">
        <h3>Resume</h3>
        <p style={{ color: '#6b7288', fontSize: 13 }}>Upload your resume — we'll extract your skills automatically. You'll also need to attach a resume each time you apply to a job.</p>
        <input type="file" accept=".pdf,.doc,.docx,.txt" onChange={handleResumeUpload} disabled={uploading} />
        {profile.resumeFilePath && (
          <div style={{ marginTop: 8 }}>
            <a href={fileUrl(profile.resumeFilePath)} target="_blank" rel="noreferrer">View uploaded resume</a>
          </div>
        )}
      </div>

      <div className="card">
        <h3>Details</h3>
        <form onSubmit={handleSave}>
          <div className="grid grid-2">
            <div className="form-group"><label>Branch</label><input value={form.branch || ''} onChange={(e) => setForm({ ...form, branch: e.target.value })} /></div>
            <div className="form-group"><label>Graduation Year</label><input type="number" value={form.graduationYear || ''} onChange={(e) => setForm({ ...form, graduationYear: e.target.value })} /></div>
          </div>
          <div className="form-group"><label>Skills (comma-separated)</label><input value={form.skills || ''} onChange={(e) => setForm({ ...form, skills: e.target.value })} /></div>
          <div className="form-group"><label>Bio</label><textarea rows={3} value={form.bio || ''} onChange={(e) => setForm({ ...form, bio: e.target.value })} /></div>
          <button className="btn btn-primary" type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save Profile'}</button>
        </form>
      </div>
    </>
  );
}

/* ================= DIRECTORY TAB ================= */
function DirectoryTab() {
  const [filters, setFilters] = useState({ company: '', domain: '', skill: '', location: '', graduationYear: '' });
  const [results, setResults] = useState([]);
  const [error, setError] = useState('');
  const [requestModal, setRequestModal] = useState(null);
  const [message, setMessage] = useState('');
  const [success, setSuccess] = useState('');

  const search = useCallback(async () => {
    try {
      const params = {};
      Object.entries(filters).forEach(([k, v]) => { if (v) params[k] = v; });
      const { data } = await api.get('/alumni/directory', { params });
      setResults(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, [filters]);

  useEffect(() => { search(); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const sendRequest = async () => {
    try {
      await api.post('/student/mentorship/request', { alumniId: requestModal.userId, message });
      setSuccess(`Mentorship request sent to ${requestModal.name}.`);
      setRequestModal(null);
      setMessage('');
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <>
      <div className="topbar"><h2>Alumni Directory</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}
      <div className="card">
        <div className="grid grid-4">
          <input placeholder="Company" value={filters.company} onChange={(e) => setFilters({ ...filters, company: e.target.value })} />
          <input placeholder="Domain" value={filters.domain} onChange={(e) => setFilters({ ...filters, domain: e.target.value })} />
          <input placeholder="Skill" value={filters.skill} onChange={(e) => setFilters({ ...filters, skill: e.target.value })} />
          <input placeholder="Location" value={filters.location} onChange={(e) => setFilters({ ...filters, location: e.target.value })} />
        </div>
        <button className="btn btn-primary" style={{ marginTop: 12 }} onClick={search}>Search</button>
      </div>
      <div className="grid grid-2">
        {results.map((a) => (
          <div key={a.id} className="card">
            <h3 style={{ marginBottom: 4 }}>{a.name}</h3>
            <div style={{ color: '#6b7288', fontSize: 13, marginBottom: 8 }}>{a.currentRole} {a.company ? `at ${a.company}` : ''}</div>
            <SkillChips skillsCsv={a.skills} />
            <div style={{ fontSize: 13, marginTop: 8 }}>{a.location} {a.graduationYear ? `· Class of ${a.graduationYear}` : ''}</div>
            <div style={{ marginTop: 10 }}>
              {a.availableForMentorship ? (
                <button className="btn btn-outline btn-sm" onClick={() => setRequestModal(a)}>Request Mentorship</button>
              ) : (
                <span style={{ fontSize: 12, color: '#8a90a6' }}>Not available for mentorship</span>
              )}
            </div>
          </div>
        ))}
      </div>

      {requestModal && (
        <Modal title={`Request mentorship from ${requestModal.name}`} onClose={() => setRequestModal(null)}>
          <div className="form-group">
            <label>Message</label>
            <textarea rows={4} value={message} onChange={(e) => setMessage(e.target.value)} placeholder="Introduce yourself and what you'd like guidance on..." />
          </div>
          <button className="btn btn-primary" onClick={sendRequest}>Send Request</button>
        </Modal>
      )}
    </>
  );
}

/* ================= JOBS TAB ================= */
function JobsTab() {
  const [jobs, setJobs] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [applyJob, setApplyJob] = useState(null);
  const [resumeFile, setResumeFile] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    try {
      const { data } = await api.get('/student/jobs');
      setJobs(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const apply = async () => {
    if (!resumeFile) { setError('Please attach a resume.'); return; }
    setSubmitting(true);
    setError('');
    try {
      const fd = new FormData();
      fd.append('resume', resumeFile);
      await api.post(`/student/jobs/${applyJob.id}/apply`, fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setSuccess(`Applied to ${applyJob.title} at ${applyJob.companyName}.`);
      setApplyJob(null);
      setResumeFile(null);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <div className="topbar"><h2>Job Portal</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}
      <div className="grid grid-2">
        {jobs.length === 0 && <div className="empty-state">No open jobs right now — check back soon.</div>}
        {jobs.map((j) => (
          <div key={j.id} className="card">
            <div className="flex-between">
              <h3 style={{ marginBottom: 4 }}>{j.title}</h3>
              <span className="badge badge-approved">{j.type}</span>
            </div>
            <div style={{ color: '#6b7288', fontSize: 13, marginBottom: 8 }}>{j.companyName} · {j.location}</div>
            <p style={{ fontSize: 14 }}>{j.description}</p>
            <SkillChips skillsCsv={j.requiredSkills} />
            <div style={{ marginTop: 12 }}>
              <button className="btn btn-primary btn-sm" onClick={() => setApplyJob(j)}>Apply Now</button>
              {j.applyLink && (
                <a className="btn btn-outline btn-sm" style={{ marginLeft: 8 }} href={j.applyLink} target="_blank" rel="noreferrer">External Link</a>
              )}
            </div>
          </div>
        ))}
      </div>

      {applyJob && (
        <Modal title={`Apply to ${applyJob.title}`} onClose={() => setApplyJob(null)}>
          <p style={{ fontSize: 13, color: '#6b7288' }}>Attach your resume — we'll compute a skill match score automatically.</p>
          <input type="file" accept=".pdf,.doc,.docx,.txt" onChange={(e) => setResumeFile(e.target.files[0])} />
          <button className="btn btn-primary" style={{ marginTop: 14 }} onClick={apply} disabled={submitting}>
            {submitting ? 'Submitting...' : 'Submit Application'}
          </button>
        </Modal>
      )}
    </>
  );
}

/* ================= APPLICATIONS TAB ================= */
function ApplicationsTab() {
  const [applications, setApplications] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/student/applications').then((res) => setApplications(res.data)).catch((err) => setError(getErrorMessage(err)));
  }, []);

  return (
    <>
      <div className="topbar"><h2>My Applications</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      {applications.length === 0 ? (
        <div className="empty-state">You haven't applied to any jobs yet.</div>
      ) : (
        <table>
          <thead><tr><th>Job</th><th>Company</th><th>Match</th><th>Status</th><th>Applied On</th></tr></thead>
          <tbody>
            {applications.map((a) => (
              <tr key={a.id}>
                <td>{a.jobTitle}</td>
                <td>{a.companyName}</td>
                <td>{a.matchScore}% <Badge status={a.matchCategory} /></td>
                <td><Badge status={a.status} /></td>
                <td>{new Date(a.appliedAt).toLocaleDateString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}

/* ================= MENTORSHIP TAB ================= */
function MentorshipTab() {
  const [requests, setRequests] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/student/mentorship/requests').then((res) => setRequests(res.data)).catch((err) => setError(getErrorMessage(err)));
  }, []);

  return (
    <>
      <div className="topbar"><h2>My Mentorship Requests</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      {requests.length === 0 ? (
        <div className="empty-state">You haven't sent any mentorship requests. Find a mentor in the Alumni Directory!</div>
      ) : (
        requests.map((r) => (
          <div key={r.id} className="card">
            <div className="flex-between">
              <strong>{r.alumniName}</strong>
              <Badge status={r.status} />
            </div>
            <div style={{ fontSize: 13, color: '#6b7288', marginTop: 4 }}>{r.message}</div>
            {r.scheduledAt && <div style={{ fontSize: 13, marginTop: 6 }}>Scheduled: {new Date(r.scheduledAt).toLocaleString()}</div>}
            {(r.status === 'ACCEPTED' || r.status === 'SCHEDULED') && (
              <div className="alert alert-success" style={{ marginTop: 10, fontSize: 13 }}>
                Your mentor accepted! Head to the Messages tab to start chatting.
              </div>
            )}
          </div>
        ))
      )}
    </>
  );
}

/* ================= MESSAGES TAB ================= */
function MessagesTab() {
  const { user } = useAuth();
  return <ChatPanel currentUserId={user.id} />;
}

/* ================= EVENTS TAB ================= */
function EventsTab() {
  const [events, setEvents] = useState([]);
  const [materials, setMaterials] = useState([]);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      const [evRes, matRes] = await Promise.all([
        api.get('/student/events'),
        api.get('/student/materials'),
      ]);
      setEvents(evRes.data);
      setMaterials(matRes.data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const register = async (id) => {
    try {
      await api.post(`/student/events/${id}/register`);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <>
      <div className="topbar"><h2>Events & Materials</h2></div>
      {error && <div className="alert alert-error">{error}</div>}

      <h3>Upcoming Events</h3>
      <div className="grid grid-2">
        {events.length === 0 && <div className="empty-state">No events scheduled.</div>}
        {events.map((ev) => (
          <div key={ev.id} className="card">
            <h3 style={{ marginBottom: 4 }}>{ev.title}</h3>
            <div style={{ fontSize: 13, color: '#6b7288' }}>{ev.eventDate ? new Date(ev.eventDate).toLocaleString() : 'Date TBA'} {ev.location ? `· ${ev.location}` : ''}</div>
            <p style={{ fontSize: 14 }}>{ev.description}</p>
            <div className="flex-between">
              <span style={{ fontSize: 12, color: '#6b7288' }}>{ev.registrationCount} registered</span>
              {ev.registeredByCurrentUser ? <Badge status="REGISTERED" /> : (
                <button className="btn btn-outline btn-sm" onClick={() => register(ev.id)}>Register</button>
              )}
            </div>
          </div>
        ))}
      </div>

      <h3 style={{ marginTop: 24 }}>Downloadable Materials</h3>
      {materials.length === 0 ? (
        <div className="empty-state">No materials uploaded yet.</div>
      ) : (
        <table>
          <thead><tr><th>Title</th><th>Description</th><th></th></tr></thead>
          <tbody>
            {materials.map((m) => (
              <tr key={m.id}>
                <td>{m.title}</td>
                <td>{m.description}</td>
                <td><a href={fileUrl(m.fileUrl)} target="_blank" rel="noreferrer">Download</a></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}
