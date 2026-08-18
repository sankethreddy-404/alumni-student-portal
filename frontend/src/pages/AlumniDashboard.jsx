import React, { useEffect, useState, useCallback } from 'react';
import Sidebar from '../components/Sidebar';
import ProfileCompleteness from '../components/ProfileCompleteness';
import SkillChips from '../components/SkillChips';
import Badge from '../components/Badge';
import Modal from '../components/Modal';
import { useAuth } from '../context/AuthContext';
import api, { getErrorMessage, fileUrl } from '../api/axios';

const NAV_ITEMS = [
  { key: 'profile', label: 'My Profile' },
  { key: 'jobs', label: 'Job Portal' },
  { key: 'directory', label: 'Alumni Directory' },
  { key: 'mentorship', label: 'Mentorship Requests' },
  { key: 'messages', label: 'Messages' },
  { key: 'events', label: 'Events' },
  { key: 'contributions', label: 'Contribution Dashboard' },
];

export default function AlumniDashboard() {
  const [tab, setTab] = useState('profile');
  return (
    <div className="dashboard">
      <Sidebar items={NAV_ITEMS} active={tab} onSelect={setTab} roleLabel="Alumni" />
      <main className="main-content">
        <div className="tab-content">
          {tab === 'profile' && <ProfileTab />}
          {tab === 'jobs' && <JobsTab />}
          {tab === 'directory' && <DirectoryTab />}
          {tab === 'mentorship' && <MentorshipTab />}
          {tab === 'messages' && <MessagesTab />}
          {tab === 'events' && <EventsTab />}
          {tab === 'contributions' && <ContributionsTab />}
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
      const { data } = await api.get('/alumni/profile');
      setProfile(data);
      setForm(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleChange = (field, value) => setForm((f) => ({ ...f, [field]: value }));

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const { data } = await api.put('/alumni/profile', {
        company: form.company, domain: form.domain, skills: form.skills,
        location: form.location, graduationYear: form.graduationYear ? Number(form.graduationYear) : null,
        currentRole: form.currentRole, experience: form.experience !== '' && form.experience != null ? Number(form.experience) : null,
        achievements: form.achievements, bio: form.bio, linkedinUrl: form.linkedinUrl,
        availableForMentorship: !!form.availableForMentorship,
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

  const handleVerify = async () => {
    try {
      await api.post('/alumni/profile/verify');
      setSuccess('Profile verified — thanks for confirming your details are up to date!');
      load();
    } catch (err) {
      setError(getErrorMessage(err));
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
      const { data } = await api.post('/alumni/profile/autofill/resume', fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setProfile(data);
      setForm(data);
      setSuccess('Resume processed — empty fields were auto-filled from your resume.');
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
        <ProfileCompleteness percentage={profile.profileCompleteness} missingFields={profile.missingFields} />
        <div className="flex-between" style={{ marginTop: 12 }}>
          <span style={{ fontSize: 13, color: '#6b7288' }}>
            Last verified: {profile.lastVerifiedAt ? new Date(profile.lastVerifiedAt).toLocaleDateString() : 'Never'}
          </span>
          <button className="btn btn-success btn-sm" onClick={handleVerify}>Confirm profile is up to date</button>
        </div>
      </div>

      <div className="card">
        <h3>Auto-fill from Resume</h3>
        <p style={{ color: '#6b7288', fontSize: 13 }}>Upload your resume (PDF/DOCX) — we'll extract skills, company, role and experience to fill in any empty fields.</p>
        <input type="file" accept=".pdf,.doc,.docx,.txt" onChange={handleResumeUpload} disabled={uploading} />
        {uploading && <span style={{ marginLeft: 10, fontSize: 13 }}>Processing...</span>}
      </div>

      <div className="card">
        <h3>Professional Details</h3>
        <form onSubmit={handleSave}>
          <div className="grid grid-2">
            <div className="form-group">
              <label>Company</label>
              <input value={form.company || ''} onChange={(e) => handleChange('company', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Current Role</label>
              <input value={form.currentRole || ''} onChange={(e) => handleChange('currentRole', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Domain</label>
              <input value={form.domain || ''} onChange={(e) => handleChange('domain', e.target.value)} placeholder="e.g. Software Engineering" />
            </div>
            <div className="form-group">
              <label>Location</label>
              <input value={form.location || ''} onChange={(e) => handleChange('location', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Graduation Year</label>
              <input type="number" value={form.graduationYear || ''} onChange={(e) => handleChange('graduationYear', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Experience (years)</label>
              <input type="number" min="0" value={form.experience ?? ''} onChange={(e) => handleChange('experience', e.target.value)} />
            </div>
            <div className="form-group">
              <label>LinkedIn URL</label>
              <input value={form.linkedinUrl || ''} onChange={(e) => handleChange('linkedinUrl', e.target.value)} placeholder="https://linkedin.com/in/..." />
            </div>
          </div>
          <div className="form-group">
            <label>Skills (comma-separated)</label>
            <input value={form.skills || ''} onChange={(e) => handleChange('skills', e.target.value)} placeholder="java, spring boot, aws" />
          </div>
          <div className="form-group">
            <label>Bio</label>
            <textarea rows={3} value={form.bio || ''} onChange={(e) => handleChange('bio', e.target.value)} />
          </div>
          <div className="form-group">
            <label>Achievements</label>
            <textarea rows={2} value={form.achievements || ''} onChange={(e) => handleChange('achievements', e.target.value)} />
          </div>
          <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              type="checkbox"
              style={{ width: 'auto' }}
              checked={!!form.availableForMentorship}
              onChange={(e) => handleChange('availableForMentorship', e.target.checked)}
            />
            <label style={{ margin: 0 }}>Available for Mentorship</label>
          </div>
          <button className="btn btn-primary" type="submit" disabled={saving}>
            {saving ? 'Saving...' : 'Save Profile'}
          </button>
        </form>
      </div>
    </>
  );
}

/* ================= JOBS TAB ================= */
function JobsTab() {
  const [jobs, setJobs] = useState([]);
  const [form, setForm] = useState({ companyName: '', title: '', description: '', requiredSkills: '', experienceRequired: '', location: '', applyLink: '', type: 'JOB' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [applicantsModal, setApplicantsModal] = useState(null); // jobId

  const load = useCallback(async () => {
    try {
      const { data } = await api.get('/alumni/jobs');
      setJobs(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handlePost = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await api.post('/alumni/jobs', {
        ...form,
        experienceRequired: form.experienceRequired !== '' ? Number(form.experienceRequired) : null,
      });
      setSuccess('Job submitted for admin approval.');
      setForm({ companyName: '', title: '', description: '', requiredSkills: '', experienceRequired: '', location: '', applyLink: '', type: 'JOB' });
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <>
      <div className="topbar"><h2>Job Portal</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="card">
        <h3>Post a Job / Internship</h3>
        <form onSubmit={handlePost}>
          <div className="grid grid-2">
            <div className="form-group">
              <label>Company Name</label>
              <input required value={form.companyName} onChange={(e) => setForm({ ...form, companyName: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Job/Internship Title</label>
              <input required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Type</label>
              <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
                <option value="JOB">Job</option>
                <option value="INTERNSHIP">Internship</option>
              </select>
            </div>
            <div className="form-group">
              <label>Location</label>
              <input value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Experience Required (years)</label>
              <input type="number" min="0" value={form.experienceRequired} onChange={(e) => setForm({ ...form, experienceRequired: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Apply Link (optional — external or leave blank for in-portal)</label>
              <input value={form.applyLink} onChange={(e) => setForm({ ...form, applyLink: e.target.value })} />
            </div>
          </div>
          <div className="form-group">
            <label>Required Skills (comma-separated)</label>
            <input required value={form.requiredSkills} onChange={(e) => setForm({ ...form, requiredSkills: e.target.value })} placeholder="java, spring boot, mysql" />
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea rows={3} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>
          <button className="btn btn-primary" type="submit">Submit for Approval</button>
        </form>
      </div>

      <div className="card">
        <h3>My Posted Jobs</h3>
        {jobs.length === 0 ? (
          <div className="empty-state">You haven't posted any jobs yet.</div>
        ) : (
          <table>
            <thead>
              <tr><th>Title</th><th>Company</th><th>Status</th><th>Applicants</th><th></th></tr>
            </thead>
            <tbody>
              {jobs.map((j) => (
                <tr key={j.id}>
                  <td>{j.title}</td>
                  <td>{j.companyName}</td>
                  <td><Badge status={j.status} /></td>
                  <td>{j.applicantCount}</td>
                  <td>
                    {j.status === 'APPROVED' && (
                      <button className="btn btn-outline btn-sm" onClick={() => setApplicantsModal(j.id)}>View Applicants</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {applicantsModal && (
        <ApplicantsModal jobId={applicantsModal} onClose={() => setApplicantsModal(null)} />
      )}
    </>
  );
}

function ApplicantsModal({ jobId, onClose }) {
  const [applicants, setApplicants] = useState([]);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      const { data } = await api.get(`/alumni/jobs/${jobId}/applicants`);
      setApplicants(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, [jobId]);

  useEffect(() => { load(); }, [load]);

  const act = async (applicationId, action) => {
    try {
      await api.post(`/alumni/applications/${applicationId}/${action}`);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const autoShortlistTop3 = async () => {
    try {
      await api.post(`/alumni/jobs/${jobId}/shortlist-top/3`);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <Modal title="Ranked Applicants" onClose={onClose}>
      {error && <div className="alert alert-error">{error}</div>}
      <div style={{ marginBottom: 12 }}>
        <button className="btn btn-outline btn-sm" onClick={autoShortlistTop3}>Auto-shortlist Top 3</button>
      </div>
      {applicants.length === 0 ? (
        <div className="empty-state">No applications yet.</div>
      ) : (
        applicants.map((a) => (
          <div key={a.id} className="card" style={{ marginBottom: 10 }}>
            <div className="flex-between">
              <div>
                <strong>#{a.rank} {a.studentName}</strong>
                <div style={{ fontSize: 12, color: '#6b7288' }}>{a.studentEmail}</div>
              </div>
              <Badge status={a.matchCategory} />
            </div>
            <div style={{ fontSize: 13, margin: '6px 0' }}>Match score: <strong>{a.matchScore}%</strong></div>
            <div className="flex-between">
              <Badge status={a.status} />
              <div style={{ display: 'flex', gap: 6 }}>
                <a className="btn btn-outline btn-sm" href={fileUrl(a.resumeFilePath)} target="_blank" rel="noreferrer">Resume</a>
                <button className="btn btn-success btn-sm" onClick={() => act(a.id, 'refer')}>Refer</button>
                <button className="btn btn-danger btn-sm" onClick={() => act(a.id, 'reject')}>Reject</button>
              </div>
            </div>
          </div>
        ))
      )}
    </Modal>
  );
}

/* ================= DIRECTORY TAB ================= */
function DirectoryTab() {
  const [filters, setFilters] = useState({ company: '', domain: '', skill: '', location: '', graduationYear: '' });
  const [results, setResults] = useState([]);
  const [error, setError] = useState('');

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

  return (
    <>
      <div className="topbar"><h2>Alumni Directory</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
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
            {a.availableForMentorship && <span className="badge badge-approved" style={{ marginTop: 8, display: 'inline-block' }}>Open to Mentorship</span>}
          </div>
        ))}
      </div>
    </>
  );
}

/* ================= MENTORSHIP TAB ================= */
function MentorshipTab() {
  const [requests, setRequests] = useState([]);
  const [error, setError] = useState('');
  const [scheduleFor, setScheduleFor] = useState(null);
  const [scheduleTime, setScheduleTime] = useState('');

  const load = useCallback(async () => {
    try {
      const { data } = await api.get('/alumni/mentorship/requests');
      setRequests(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const act = async (id, action) => {
    try {
      await api.post(`/alumni/mentorship/requests/${id}/${action}`);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const submitSchedule = async () => {
    try {
      await api.post(`/alumni/mentorship/requests/${scheduleFor}/schedule`, { scheduledAt: scheduleTime });
      setScheduleFor(null);
      setScheduleTime('');
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <>
      <div className="topbar"><h2>Mentorship Requests</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      {requests.length === 0 ? (
        <div className="empty-state">No mentorship requests yet.</div>
      ) : (
        requests.map((r) => (
          <div key={r.id} className="card">
            <div className="flex-between">
              <div>
                <strong>{r.studentName}</strong>
                <div style={{ fontSize: 13, color: '#6b7288' }}>{r.message}</div>
              </div>
              <Badge status={r.status} />
            </div>
            {r.scheduledAt && <div style={{ fontSize: 13, marginTop: 6 }}>Scheduled: {new Date(r.scheduledAt).toLocaleString()}</div>}
            <div style={{ marginTop: 10, display: 'flex', gap: 6 }}>
              {r.status === 'PENDING' && (
                <>
                  <button className="btn btn-success btn-sm" onClick={() => act(r.id, 'accept')}>Accept</button>
                  <button className="btn btn-danger btn-sm" onClick={() => act(r.id, 'reject')}>Reject</button>
                </>
              )}
              {(r.status === 'ACCEPTED' || r.status === 'SCHEDULED') && (
                <>
                  <button className="btn btn-outline btn-sm" onClick={() => setScheduleFor(r.id)}>
                    {r.status === 'SCHEDULED' ? 'Reschedule' : 'Schedule'}
                  </button>
                  <button className="btn btn-success btn-sm" onClick={() => act(r.id, 'complete')}>Mark Completed</button>
                </>
              )}
            </div>
          </div>
        ))
      )}

      {scheduleFor && (
        <Modal title="Schedule Mentorship Session" onClose={() => setScheduleFor(null)}>
          <div className="form-group">
            <label>Date & Time</label>
            <input type="datetime-local" value={scheduleTime} onChange={(e) => setScheduleTime(e.target.value)} />
          </div>
          <button className="btn btn-primary" onClick={submitSchedule}>Confirm Schedule</button>
        </Modal>
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
  const [error, setError] = useState('');
  const [form, setForm] = useState({ title: '', description: '', eventDate: '', location: '' });

  const load = useCallback(async () => {
    try {
      const { data } = await api.get('/events');
      setEvents(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const create = async (e) => {
    e.preventDefault();
    try {
      await api.post('/events', form);
      setForm({ title: '', description: '', eventDate: '', location: '' });
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const register = async (id) => {
    try {
      await api.post(`/events/${id}/register`);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <>
      <div className="topbar"><h2>Events</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        <h3>Create an Event</h3>
        <form onSubmit={create}>
          <div className="grid grid-2">
            <div className="form-group"><label>Title</label><input required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></div>
            <div className="form-group"><label>Location</label><input value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} /></div>
            <div className="form-group"><label>Date & Time</label><input type="datetime-local" value={form.eventDate} onChange={(e) => setForm({ ...form, eventDate: e.target.value })} /></div>
          </div>
          <div className="form-group"><label>Description</label><textarea rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></div>
          <button className="btn btn-primary" type="submit">Create Event</button>
        </form>
      </div>
      <div className="grid grid-2">
        {events.map((ev) => (
          <div key={ev.id} className="card">
            <h3 style={{ marginBottom: 4 }}>{ev.title}</h3>
            <div style={{ fontSize: 13, color: '#6b7288' }}>{ev.eventDate ? new Date(ev.eventDate).toLocaleString() : 'Date TBA'} {ev.location ? `· ${ev.location}` : ''}</div>
            <p style={{ fontSize: 14 }}>{ev.description}</p>
            <div className="flex-between">
              <span style={{ fontSize: 12, color: '#6b7288' }}>{ev.registrationCount} registered</span>
              {ev.registeredByCurrentUser ? (
                <Badge status="REGISTERED" />
              ) : (
                <button className="btn btn-outline btn-sm" onClick={() => register(ev.id)}>Register</button>
              )}
            </div>
          </div>
        ))}
      </div>
    </>
  );
}

/* ================= CONTRIBUTIONS TAB ================= */
function ContributionsTab() {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/alumni/contributions').then((res) => setData(res.data)).catch((err) => setError(getErrorMessage(err)));
  }, []);

  return (
    <>
      <div className="topbar"><h2>Contribution Dashboard</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      {data && (
        <div className="grid grid-4">
          <div className="stat-card"><div className="label">Jobs Posted</div><div className="value">{data.jobsPosted}</div></div>
          <div className="stat-card"><div className="label">Mentorship Sessions</div><div className="value">{data.mentorshipSessions}</div></div>
          <div className="stat-card"><div className="label">Events Attended</div><div className="value">{data.eventsAttended}</div></div>
          <div className="stat-card"><div className="label">Total Contribution Score</div><div className="value">{data.totalScore}</div></div>
        </div>
      )}
    </>
  );
}

/* ================= SHARED CHAT PANEL (used by both Alumni & Student dashboards) ================= */
export function ChatPanel({ currentUserId }) {
  const [conversations, setConversations] = useState([]);
  const [activePartner, setActivePartner] = useState(null);
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState('');
  const [error, setError] = useState('');

  const loadInbox = useCallback(async () => {
    try {
      const { data } = await api.get('/messages/inbox');
      setConversations(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => { loadInbox(); }, [loadInbox]);

  const loadConversation = useCallback(async (partnerId) => {
    try {
      const { data } = await api.get(`/messages/conversation/${partnerId}`);
      setMessages(data);
      setActivePartner(partnerId);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  // Light polling so new messages show up without a full page refresh
  useEffect(() => {
    if (!activePartner) return;
    const interval = setInterval(() => loadConversation(activePartner), 4000);
    return () => clearInterval(interval);
  }, [activePartner, loadConversation]);

  const send = async () => {
    if (!text.trim() || !activePartner) return;
    try {
      await api.post('/messages', { receiverId: activePartner, content: text });
      setText('');
      loadConversation(activePartner);
      loadInbox();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <>
      <div className="topbar"><h2>Messages</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="alert alert-info" style={{ fontSize: 13 }}>
        Chat unlocks automatically once a mentorship request between you and the other person has been accepted.
      </div>
      <div className="chat-window">
        <div className="chat-list">
          {conversations.length === 0 && <div className="empty-state">No conversations yet.</div>}
          {conversations.map((c) => (
            <div
              key={c.partnerId}
              className={`chat-list-item ${activePartner === c.partnerId ? 'active' : ''}`}
              onClick={() => loadConversation(c.partnerId)}
            >
              <div className="flex-between">
                <strong>{c.partnerName}</strong>
                {c.unreadCount > 0 && <span className="badge badge-pending">{c.unreadCount}</span>}
              </div>
              <div style={{ fontSize: 12, color: '#6b7288' }}>{c.lastMessage}</div>
            </div>
          ))}
        </div>
        <div className="chat-messages">
          {!activePartner ? (
            <div className="empty-state" style={{ margin: 'auto' }}>Select a conversation to start chatting.</div>
          ) : (
            <>
              <div className="chat-messages-list">
                {messages.map((m) => (
                  <div key={m.id} className={`chat-bubble ${m.senderId === currentUserId ? 'mine' : 'theirs'}`}>
                    {m.content}
                  </div>
                ))}
              </div>
              <div className="chat-input-row">
                <input
                  value={text}
                  onChange={(e) => setText(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && send()}
                  placeholder="Type a message..."
                />
                <button className="btn btn-primary" onClick={send}>Send</button>
              </div>
            </>
          )}
        </div>
      </div>
    </>
  );
}
