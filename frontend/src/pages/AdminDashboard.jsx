import React, { useEffect, useState, useCallback } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import Sidebar from '../components/Sidebar';
import Badge from '../components/Badge';
import api, { getErrorMessage } from '../api/axios';

const NAV_ITEMS = [
  { key: 'overview', label: 'Overview' },
  { key: 'alumniApprovals', label: 'Alumni Approvals' },
  { key: 'jobApprovals', label: 'Job Approvals' },
  { key: 'users', label: 'Manage Users' },
  { key: 'profiles', label: 'Profile Monitoring' },
  { key: 'contributors', label: 'Top Contributors' },
];

const COLORS = ['#2f5dff', '#17a673', '#d9942e', '#e0384b'];

export default function AdminDashboard() {
  const [tab, setTab] = useState('overview');
  return (
    <div className="dashboard">
      <Sidebar items={NAV_ITEMS} active={tab} onSelect={setTab} roleLabel="Admin" />
      <main className="main-content">
        <div className="tab-content">
          {tab === 'overview' && <OverviewTab />}
          {tab === 'alumniApprovals' && <AlumniApprovalsTab />}
          {tab === 'jobApprovals' && <JobApprovalsTab />}
          {tab === 'users' && <UsersTab />}
          {tab === 'profiles' && <ProfilesTab />}
          {tab === 'contributors' && <ContributorsTab />}
        </div>
      </main>
    </div>
  );
}

/* ================= OVERVIEW ================= */
function OverviewTab() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/admin/dashboard').then((res) => setStats(res.data)).catch((err) => setError(getErrorMessage(err)));
  }, []);

  if (error) return <div className="alert alert-error">{error}</div>;
  if (!stats) return <div className="empty-state">Loading dashboard...</div>;

  const barData = [
    { name: 'Alumni', value: stats.totalAlumni },
    { name: 'Students', value: stats.totalStudents },
    { name: 'Jobs Posted', value: stats.jobsPosted },
    { name: 'Mentorships', value: stats.mentorshipSessions },
  ];

  const pieData = [
    { name: 'Active Alumni', value: stats.activeAlumni },
    { name: 'Pending Approval', value: stats.pendingAlumniApprovals },
  ];

  return (
    <>
      <div className="topbar"><h2>Admin Dashboard</h2></div>

      <div className="grid grid-4">
        <div className="stat-card"><div className="label">Total Alumni</div><div className="value">{stats.totalAlumni}</div></div>
        <div className="stat-card"><div className="label">Active Alumni</div><div className="value">{stats.activeAlumni}</div></div>
        <div className="stat-card"><div className="label">Total Students</div><div className="value">{stats.totalStudents}</div></div>
        <div className="stat-card"><div className="label">Jobs Posted</div><div className="value">{stats.jobsPosted}</div></div>
        <div className="stat-card"><div className="label">Pending Alumni Approvals</div><div className="value">{stats.pendingAlumniApprovals}</div></div>
        <div className="stat-card"><div className="label">Pending Job Approvals</div><div className="value">{stats.pendingJobApprovals}</div></div>
        <div className="stat-card"><div className="label">Mentorship Sessions</div><div className="value">{stats.mentorshipSessions}</div></div>
        <div className="stat-card"><div className="label">Event Participation</div><div className="value">{stats.eventParticipation}</div></div>
      </div>

      <div className="grid grid-2" style={{ marginTop: 20 }}>
        <div className="card">
          <h3>Platform Activity</h3>
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={barData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="value" fill="#2f5dff" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div className="card">
          <h3>Alumni Approval Status</h3>
          <ResponsiveContainer width="100%" height={280}>
            <PieChart>
              <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={90} label>
                {pieData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Legend />
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="card" style={{ marginTop: 20 }}>
        <h3>Average Profile Completeness</h3>
        <div className="progress-wrap" style={{ height: 18 }}>
          <div className="progress-bar" style={{ width: `${stats.averageProfileCompleteness}%` }} />
        </div>
        <div style={{ fontSize: 13, marginTop: 6, color: '#6b7288' }}>{stats.averageProfileCompleteness.toFixed(1)}% average across all alumni</div>
      </div>
    </>
  );
}

/* ================= ALUMNI APPROVALS ================= */
function AlumniApprovalsTab() {
  const [pending, setPending] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = useCallback(async () => {
    try {
      const { data } = await api.get('/admin/alumni/pending');
      setPending(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const approve = async (id) => {
    try {
      await api.post(`/admin/alumni/${id}/approve`);
      setSuccess('Alumni account approved.');
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <>
      <div className="topbar"><h2>Alumni Registration Approvals</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}
      {pending.length === 0 ? (
        <div className="empty-state">No pending alumni approvals.</div>
      ) : (
        <table>
          <thead><tr><th>Name</th><th>Email</th><th></th></tr></thead>
          <tbody>
            {pending.map((u) => (
              <tr key={u.id}>
                <td>{u.name}</td>
                <td>{u.email}</td>
                <td><button className="btn btn-success btn-sm" onClick={() => approve(u.id)}>Approve</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}

/* ================= JOB APPROVALS ================= */
function JobApprovalsTab() {
  const [pending, setPending] = useState([]);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      const { data } = await api.get('/admin/jobs/pending');
      setPending(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const act = async (id, action) => {
    try {
      await api.post(`/admin/jobs/${id}/${action}`);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <>
      <div className="topbar"><h2>Job Post Approvals</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      {pending.length === 0 ? (
        <div className="empty-state">No pending job approvals.</div>
      ) : (
        pending.map((j) => (
          <div key={j.id} className="card">
            <div className="flex-between">
              <div>
                <strong>{j.title}</strong> — {j.companyName}
                <div style={{ fontSize: 13, color: '#6b7288' }}>Posted by {j.postedByName}</div>
              </div>
              <div style={{ display: 'flex', gap: 6 }}>
                <button className="btn btn-success btn-sm" onClick={() => act(j.id, 'approve')}>Approve</button>
                <button className="btn btn-danger btn-sm" onClick={() => act(j.id, 'reject')}>Reject</button>
              </div>
            </div>
            <p style={{ fontSize: 14, marginTop: 8 }}>{j.description}</p>
          </div>
        ))
      )}
    </>
  );
}

/* ================= USERS ================= */
function UsersTab() {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      const { data } = await api.get('/admin/users');
      setUsers(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const toggle = async (id, active) => {
    try {
      await api.post(`/admin/users/${id}/${active ? 'deactivate' : 'activate'}`);
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <>
      <div className="topbar"><h2>Manage Users</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      <table>
        <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th></th></tr></thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.name}</td>
              <td>{u.email}</td>
              <td>{u.role}</td>
              <td>{u.active ? <Badge status="APPROVED" /> : <Badge status="REJECTED" />}</td>
              <td>
                {u.role !== 'ADMIN' && (
                  <button className={`btn btn-sm ${u.active ? 'btn-danger' : 'btn-success'}`} onClick={() => toggle(u.id, u.active)}>
                    {u.active ? 'Deactivate' : 'Activate'}
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}

/* ================= PROFILE MONITORING ================= */
function ProfilesTab() {
  const [incomplete, setIncomplete] = useState([]);
  const [unverified, setUnverified] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([
      api.get('/admin/profiles/incomplete', { params: { threshold: 70 } }),
      api.get('/admin/profiles/unverified'),
    ]).then(([a, b]) => {
      setIncomplete(a.data);
      setUnverified(b.data);
    }).catch((err) => setError(getErrorMessage(err)));
  }, []);

  return (
    <>
      <div className="topbar"><h2>Profile Monitoring</h2></div>
      {error && <div className="alert alert-error">{error}</div>}

      <div className="card">
        <h3>Alumni with Incomplete Profiles (&lt; 70%)</h3>
        {incomplete.length === 0 ? <div className="empty-state">Everyone's profile looks good!</div> : (
          <table>
            <thead><tr><th>Name</th><th>Email</th><th>Completeness</th></tr></thead>
            <tbody>
              {incomplete.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td>{p.email}</td>
                  <td>{p.profileCompleteness}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="card">
        <h3>Alumni Not Verified in 6+ Months</h3>
        {unverified.length === 0 ? <div className="empty-state">Everyone is up to date.</div> : (
          <table>
            <thead><tr><th>Name</th><th>Email</th><th>Last Verified</th></tr></thead>
            <tbody>
              {unverified.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td>{p.email}</td>
                  <td>{p.lastVerifiedAt ? new Date(p.lastVerifiedAt).toLocaleDateString() : 'Never'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}

/* ================= TOP CONTRIBUTORS ================= */
function ContributorsTab() {
  const [contributors, setContributors] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/admin/contributors/top', { params: { limit: 10 } })
      .then((res) => setContributors(res.data))
      .catch((err) => setError(getErrorMessage(err)));
  }, []);

  return (
    <>
      <div className="topbar"><h2>Top Contributors</h2></div>
      {error && <div className="alert alert-error">{error}</div>}
      <table>
        <thead><tr><th>#</th><th>Alumni</th><th>Jobs Posted</th><th>Mentorship Sessions</th><th>Events Attended</th><th>Score</th></tr></thead>
        <tbody>
          {contributors.map((c, i) => (
            <tr key={c.alumniId}>
              <td>{i + 1}</td>
              <td>{c.alumniName}</td>
              <td>{c.jobsPosted}</td>
              <td>{c.mentorshipSessions}</td>
              <td>{c.eventsAttended}</td>
              <td><strong>{c.totalScore}</strong></td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}
