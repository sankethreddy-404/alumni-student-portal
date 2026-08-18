import React from 'react';

export default function SkillChips({ skillsCsv }) {
  if (!skillsCsv) return <span style={{ color: '#8a90a6', fontSize: 13 }}>No skills listed</span>;
  const skills = skillsCsv.split(',').map((s) => s.trim()).filter(Boolean);
  return (
    <div className="chip-list">
      {skills.map((s, i) => (
        <span key={i} className="skill-chip">{s}</span>
      ))}
    </div>
  );
}
