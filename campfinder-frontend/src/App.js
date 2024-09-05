import React, { useState } from 'react';
import './App.css';

function App() {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [parks, setParks] = useState('');
  const [message, setMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    const parkList = parks.split(',').map(park => park.trim());
    const url = `http://localhost:8080/check-availability?startDate=${startDate}&endDate=${endDate}${parkList.map(park => `&parks=${encodeURIComponent(park)}`).join('')}`;

    try {
      const response = await fetch(url);
      const data = await response.text();
      setMessage(data);
    } catch (error) {
      setMessage('Error: Unable to connect to the server.');
    }
  };

  return (
    <div className="App">
      <h1>CampFinder</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="startDate">Start Date:</label>
          <input
            type="date"
            id="startDate"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            required
          />
        </div>
        <div>
          <label htmlFor="endDate">End Date:</label>
          <input
            type="date"
            id="endDate"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            required
          />
        </div>
        <div>
          <label htmlFor="parks">Parks (comma-separated):</label>
          <input
            type="text"
            id="parks"
            value={parks}
            onChange={(e) => setParks(e.target.value)}
            required
          />
        </div>
        <button type="submit">Check Availability</button>
      </form>
      {message && <p className="message">{message}</p>}
    </div>
  );
}

export default App;