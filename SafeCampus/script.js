import { initializeApp } from "https://www.gstatic.com/firebasejs/12.8.0/firebase-app.js";
import { getFirestore, collection, onSnapshot, doc, deleteDoc, updateDoc, getDoc } 
from "https://www.gstatic.com/firebasejs/12.8.0/firebase-firestore.js";
import { getAuth, signInWithEmailAndPassword, onAuthStateChanged, signOut }
from "https://www.gstatic.com/firebasejs/12.8.0/firebase-auth.js";


const firebaseConfig = {
  apiKey: "AIzaSyBsTqzgQVUUaqkVj5oJogY55PSowyabB4c",
  authDomain: "safe-campus-84335.firebaseapp.com",
  projectId: "safe-campus-84335",
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);
const auth = getAuth(app);

// LOGIN FUNCTION
window.loginAdmin = () => {
  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  signInWithEmailAndPassword(auth, email, password)
    .then(() => {
      alert("Login success!");
      document.getElementById("loginPage").style.display = "none";
      document.getElementById("adminPage").style.display = "block";
    })
    .catch(err => {
      alert("Login failed: " + err.message);
    });
};

// LOGOUT
window.logoutAdmin = () => {
  signOut(auth);
};

// CHECK LOGIN STATUS
onAuthStateChanged(auth, (user) => {
  if(user){
    document.getElementById("loginPage").style.display = "none";
    document.getElementById("adminPage").style.display = "block";
  } else {
    document.getElementById("loginPage").style.display = "block";
    document.getElementById("adminPage").style.display = "none";
  }
});

window.showSection = (id) => {
  document.querySelectorAll('.section').forEach(sec => sec.classList.remove('active'));
  document.getElementById(id).classList.add('active');
};

onSnapshot(collection(db, "locations"), (snapshot) => {

  const locationsBody = document.getElementById("locationsBody");
  const reportsBody = document.getElementById("reportsBody");

  locationsBody.innerHTML = "";
  reportsBody.innerHTML = "";

  let locCount = 0;
  let reportCount = 0;
  let accidentCount = 0;

  snapshot.forEach(docSnap => {
    const d = docSnap.data();
    const id = docSnap.id;
    locCount++;

    // LOCATIONS TABLE
    locationsBody.innerHTML += `
      <tr>
        <td>${locCount}</td>
        <td>${d.username || "-"}</td>
        <td>${d.type || "-"}</td>
        <td>${d.latitude || "-"}</td>
        <td>${d.longitude || "-"}</td>
      </tr>
    `;

    // REPORTS TABLE
    reportCount++;
    if(d.type === "Accident") accidentCount++;

    reportsBody.innerHTML += `
      <tr>
        <td>${reportCount}</td>
        <td>${d.username || "-"}</td>
        <td>${d.timestamp || "-"}</td>
        <td>${d.type || "-"}</td>
        <td>${d.latitude || "-"}, ${d.longitude || "-"}</td>
        <td>
          <button class="view" onclick="viewDetails('${id}')">View</button>
          <button class="edit" onclick="editReport('${id}')">Edit</button>
          <button class="delete" onclick="deleteReport('${id}')">Delete</button>
        </td>
      </tr>
    `;
  });

  // DASHBOARD COUNTERS
  document.getElementById("totalReports").innerText = reportCount;
  document.getElementById("totalAccidents").innerText = accidentCount;
});

// VIEW DETAILS
window.viewDetails = async (id) => {
  const snap = await getDoc(doc(db, "locations", id));
  const d = snap.data();

  document.getElementById("mUser").innerText = d.username;
  document.getElementById("mType").innerText = d.type;
  document.getElementById("mTime").innerText = d.timestamp;
  document.getElementById("mLat").innerText = d.latitude;
  document.getElementById("mLong").innerText = d.longitude;
  document.getElementById("mDesc").innerText = d.description;

  document.getElementById("detailModal").style.display = "block";
};

window.closeModal = () => {
  document.getElementById("detailModal").style.display = "none";
};

// DELETE
window.deleteReport = async (id) => {
  if(confirm("Delete this report?")){
    await deleteDoc(doc(db, "locations", id));
  }
};

// EDIT TYPE
window.editReport = async (id) => {
  const newType = prompt("Enter new type:");
  if(newType){
    await updateDoc(doc(db, "locations", id), { type: newType });
  }
};
