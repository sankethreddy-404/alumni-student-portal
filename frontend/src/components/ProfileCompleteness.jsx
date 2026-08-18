import React from 'react';

const LABELS = {
  company: 'Company',
  skills: 'Skills',
  location: 'Location',
  domain: 'Domain',
  experience: 'Experience',
  bio: 'Bio',
  linkedinUrl: 'LinkedIn URL',
};

export default function ProfileCompleteness({ percentage, missingFields = [] }) {
  const pct = percentage ?? 0;
  const color = pct >= 80 ? '#17a673' : pct >= 50 ? '#2f5dff' : '#d9942e';

  return (
    <div>
      <div className="flex-between">
        <strong>Profile {pct}% Complete</strong>
      </div>
      <div className="progress-wrap">
        <div className="progress-bar" style={{ width: `${pct}%`, background: color }} />
      </div>
      {missingFields.length > 0 && (
        <div style={{ marginTop: 10 }}>
          <span style={{ fontSize: 13, color: '#6b7288' }}>Missing: </span>
          {missingFields.map((f) => (
            <span key={f} className="missing-tag">{LABELS[f] || f}</span>
          ))}
        </div>
      )}
    </div>
  );
}
