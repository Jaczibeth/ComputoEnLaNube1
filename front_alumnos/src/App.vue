<template>
  <!-- PANTALLA DE INICIO -->
  <div v-if="pantalla==='inicio'" class="hero-premium d-flex align-items-center justify-content-center">
    <div class="text-center text-white">
      <h1 class="display-3 fw-bold mb-3">Sistema JM</h1>
      <p class="lead mb-4">Bienvenidos a nuestro sistema </p>
      <button class="btn btn-light btn-lg rounded-pill px-5 shadow"
              @click="pantalla='sistema'">
        Ingresar
      </button>
    </div>
  </div>

  <!-- PANTALLA DEL SISTEMA -->
  <div v-if="pantalla==='sistema'" class="sistema-bg min-vh-100 p-5">
    <div class="container">

      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="fw-bold">BIENVENIDO A JM</h2>
        <button class="btn btn-primary rounded-pill px-4"
                @click="mostrarModal=true">
          Registrar Alumno
        </button>
      </div>

      <!-- TARJETAS DE ESTADÍSTICAS -->
      <div class="row g-4 mb-5">
        <div class="col-md-3">
          <div class="stat-card text-center p-4">
            <h3>{{ totalAlumnos }}</h3>
            <p>Total General</p>
          </div>
        </div>

        <div v-for="c in carreras" :key="c" class="col-md-3">
          <div class="stat-card text-center p-4">
            <h5>{{ totalPorCarrera(c) }}</h5>
            <small>{{ c }}</small>
          </div>
        </div>
      </div>

      <!-- FILTRO POR CARRERA -->
      <div class="mb-4">
        <button v-for="c in carreras"
                :key="c"
                class="btn m-1 rounded-pill"
                :class="carreraSeleccionada===c?'btn-dark':'btn-outline-dark'"
                @click="carreraSeleccionada=c">
          {{ c }}
        </button>
      </div>

      <!-- BUSCADOR -->
      <input v-if="carreraSeleccionada"
             class="form-control mb-4 shadow-sm"
             placeholder="Buscar alumno..."
             v-model="busqueda">

      <!-- LISTA DE ALUMNOS -->
      <transition-group name="fade" tag="div" class="row g-4">
        <div v-for="alumno in alumnosFiltrados"
             :key="alumno.id"
             class="col-12">

          <div class="alumno-card-horizontal p-4">

            <div class="d-flex align-items-center">

              <div class="avatar-animado me-4">
                <img :src="alumno.imagenURL || 'https://cdn-icons-png.flaticon.com/512/3135/3135715.png'">
              </div>

              <div class="flex-fill">
                <h5 class="fw-bold mb-1">{{ alumno.nombre }} {{ alumno.apellido }}</h5>
                <p class="text-muted mb-1">🎓 Número de Control: {{ alumno.numeroControl }}</p>
                <p class="text-muted mb-1">✉ {{ alumno.email }}</p>
                <span class="badge mb-2" :class="coloresCarrera[alumno.carrera]">
                  {{ alumno.carrera }}
                </span>
                <p class="text-muted mb-2">📞 {{ alumno.telefono }}</p>

                <button class="btn btn-warning btn-sm" @click="editarAlumno(alumno)">Editar</button>
                <button class="btn btn-danger btn-sm mx-2" @click="eliminarAlumno(alumno.id)">Eliminar</button>
              </div>
            </div>
          </div>
        </div>
      </transition-group>
    </div>
  </div>

  <!-- MODAL DE REGISTRO/EDICIÓN -->
  <div v-if="mostrarModal" class="modal-overlay">
    <div class="modal-premium">
      <h4 class="fw-bold mb-4 text-center">
        {{ editado ? 'Editar Alumno' : 'Registrar Alumno' }}
      </h4>

      <form @submit.prevent="guardarAlumno">

        <input maxlength="8"
               class="form-control mb-3"
               placeholder="Número de Control (8 dígitos)"
               v-model="nuevoAlumno.numeroControl"
               @input="nuevoAlumno.numeroControl = nuevoAlumno.numeroControl.replace(/[^0-9]/g,'')">

        <input maxlength="50"
               class="form-control mb-3"
               placeholder="Nombres"
               v-model="nuevoAlumno.nombre"
               @input="nuevoAlumno.nombre = nuevoAlumno.nombre.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g,'')">

        <input maxlength="50"
               class="form-control mb-3"
               placeholder="Apellidos"
               v-model="nuevoAlumno.apellido"
               @input="nuevoAlumno.apellido = nuevoAlumno.apellido.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g,'')">

        <select class="form-select mb-3" v-model="nuevoAlumno.carrera">
          <option disabled value="">Seleccione Carrera</option>
          <option v-for="c in carreras" :key="c">{{ c }}</option>
        </select>

        <input type="email"
               class="form-control mb-3"
               placeholder="Correo electrónico"
               v-model="nuevoAlumno.email"
               @input="nuevoAlumno.email = nuevoAlumno.email.toLowerCase()">

        <input maxlength="10"
               class="form-control mb-3"
               placeholder="Teléfono (10 dígitos)"
               v-model="nuevoAlumno.telefono"
               @input="nuevoAlumno.telefono = nuevoAlumno.telefono.replace(/[^0-9]/g,'')">

        <input class="form-control mb-4" placeholder="URL Imagen" v-model="nuevoAlumno.imagenURL">

        <div class="text-center">
          <button class="btn btn-success rounded-pill px-4 me-2">Guardar</button>
          <button type="button" class="btn btn-secondary rounded-pill px-4" @click="cerrarModal">Cancelar</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Swal from 'sweetalert2'

const pantalla = ref('inicio')
const mostrarModal = ref(false)
const carreraSeleccionada = ref(null)
const busqueda = ref('')
const editado = ref(false)

const alumnos = ref([])

const carreras = [
  'Contador Público',
  'Ingeniería en Sistemas Computacionales',
  'Ingeniería Civil',
  'Ingeniería Industrial',
  'Ingeniería Mecatrónica',
  'Licenciatura en Administración',
  'Licenciatura en Arquitectura',
  'Ingeniería en Gestión Empresarial'
]

const coloresCarrera = {
  'Contador Público': 'badge-cp',
  'Ingeniería en Sistemas Computacionales': 'badge-sistemas',
  'Ingeniería Civil': 'badge-civil',
  'Ingeniería Industrial': 'badge-industrial',
  'Ingeniería Mecatrónica': 'badge-meca',
  'Licenciatura en Administración': 'badge-admin',
  'Licenciatura en Arquitectura': 'badge-arqui',
  'Ingeniería en Gestión Empresarial': 'badge-gestion'
}

const nuevoAlumno = ref({
  id: null,
  numeroControl: '',
  nombre: '',
  apellido: '',
  carrera: '',
  email: '',
  telefono: '',
  imagenURL: ''
})

// --- Cargar alumnos desde localStorage ---
onMounted(() => {
  const guardado = localStorage.getItem('alumnos')
  alumnos.value = guardado ? JSON.parse(guardado) : []
})

// --- Computed y filtros ---
const totalAlumnos = computed(() => alumnos.value.length)
const totalPorCarrera = (carrera) =>
  alumnos.value.filter(a => a.carrera === carrera).length

const alumnosFiltrados = computed(() => {
  if (!carreraSeleccionada.value) return []
  return alumnos.value
    .filter(a => a.carrera === carreraSeleccionada.value)
    .filter(a =>
      (a.nombre + ' ' + a.apellido)
        .toLowerCase()
        .includes(busqueda.value.toLowerCase())
    )
})

// --- Validaciones ---
const validarEmail = (email) => {
  return /^[^\s@]+@(gmail\.com|outlook\.com|hotmail\.com|tlaxiaco\.tecnm\.mx)$/i.test(email)
}

const validarTelefono = (telefono) => {
  return /^[0-9]{10}$/.test(telefono.replace(/\D/g,''))
}

const validarDuplicado = () => {
  const { id, numeroControl, email, telefono } = nuevoAlumno.value

  if (alumnos.value.find(a => a.numeroControl === numeroControl && a.id !== id)) {
    Swal.fire('Número de control duplicado','Este número ya está registrado.','warning')
    return true
  }

  if (alumnos.value.find(a => a.email === email && a.id !== id)) {
    Swal.fire('Email duplicado','Este correo ya pertenece a otro alumno.','warning')
    return true
  }

  if (alumnos.value.find(a => a.telefono === telefono && a.id !== id)) {
    Swal.fire('Teléfono duplicado','Este número ya pertenece a otro alumno.','warning')
    return true
  }

  return false
}

const validarAlumno = () => {
  const { numeroControl, nombre, apellido, carrera, email, telefono } = nuevoAlumno.value

  if (!numeroControl || !nombre || !apellido || !carrera || !email || !telefono) {
    Swal.fire('Error','Todos los campos son obligatorios','error')
    return false
  }

  if (!/^[0-9]{8}$/.test(numeroControl)) {
    Swal.fire('Error','Número de control debe tener 8 dígitos','error')
    return false
  }

  if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/.test(nombre)) {
    Swal.fire('Error','Nombre solo puede contener letras y espacios','error')
    return false
  }

  if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/.test(apellido)) {
    Swal.fire('Error','Apellido solo puede contener letras y espacios','error')
    return false
  }

  if (!validarEmail(email)) {
    Swal.fire('Error','Ingrese un correo válido (Gmail, Outlook/Hotmail o institucional)','error')
    return false
  }

  if (!validarTelefono(telefono)) {
    Swal.fire('Error','Ingrese un teléfono válido de México o EE. UU. (10 dígitos)','error')
    return false
  }

  if (validarDuplicado()) return false

  return true
}

// --- Funciones CRUD ---
const guardarAlumno = async () => {
  if (!validarAlumno()) return

  const confirmacion = await Swal.fire({
    title: editado.value ? '¿Guardar cambios?' : '¿Registrar alumno?',
    icon: 'question',
    showCancelButton: true
  })

  if (!confirmacion.isConfirmed) return

  if (editado.value) {
    const index = alumnos.value.findIndex(a => a.id === nuevoAlumno.value.id)
    if (index !== -1) alumnos.value[index] = { ...nuevoAlumno.value }
  } else {
    nuevoAlumno.value.id = Date.now()
    alumnos.value.push({ ...nuevoAlumno.value })
  }

  localStorage.setItem('alumnos', JSON.stringify(alumnos.value))
  cerrarModal()
}

const editarAlumno = (alumno) => {
  nuevoAlumno.value = { ...alumno }
  editado.value = true
  mostrarModal.value = true
}

const eliminarAlumno = (id) => {
  alumnos.value = alumnos.value.filter(a => a.id !== id)
  localStorage.setItem('alumnos', JSON.stringify(alumnos.value))
}

const cerrarModal = () => {
  nuevoAlumno.value = {
    id: null,
    numeroControl: '',
    nombre: '',
    apellido: '',
    carrera: '',
    email: '',
    telefono: '',
    imagenURL: ''
  }
  editado.value = false
  mostrarModal.value = false
}
</script>

<style>
.hero-premium{
  height:100vh;
  background:linear-gradient(135deg,#0a192f,#112240,#1f4068);
}
.sistema-bg{
  background:linear-gradient(135deg,#f4f6f9,#e6ecf5);
}
.stat-card{
  border-radius:25px;
  background:linear-gradient(135deg,#1f4068,#162447);
  color:white;
  transition:.3s;
  box-shadow:0 10px 25px rgba(0,0,0,.15);
}
.stat-card:hover{transform:translateY(-8px);}
.alumno-card-horizontal{
  border-radius:25px;
  background:white;
  border-left:8px solid #1f4068;
  transition:.3s;
  box-shadow:0 10px 20px rgba(0,0,0,.08);
}
.alumno-card-horizontal:hover{transform:translateY(-6px);}
.avatar-animado{
  width:120px;
  height:120px;
  border-radius:50%;
  padding:5px;
  background:linear-gradient(45deg,#0f3460,#e94560,#0f3460);
  animation:glow 4s linear infinite;
}
.avatar-animado img{
  width:100%;
  height:100%;
  border-radius:50%;
  object-fit:cover;
}
@keyframes glow{0%{filter:hue-rotate(0deg);}100%{filter:hue-rotate(360deg);}}
.modal-overlay{
  position:fixed;
  inset:0;
  background:rgba(0,0,0,.7);
  backdrop-filter:blur(5px);
  display:flex;
  align-items:center;
  justify-content:center;
  z-index:999;
}
.modal-premium{
  background:white;
  padding:45px;
  border-radius:30px;
  width:480px;
  box-shadow:0 25px 50px rgba(0,0,0,.35);
}
.badge-cp{background:#1f4068;color:white;padding:5px 10px;border-radius:12px;}
.badge-sistemas{background:#0f3460;color:white;padding:5px 10px;border-radius:12px;}
.badge-civil{background:#162447;color:white;padding:5px 10px;border-radius:12px;}
.badge-industrial{background:#1f4068;color:white;padding:5px 10px;border-radius:12px;}
.badge-meca{background:#0f3460;color:white;padding:5px 10px;border-radius:12px;}
.badge-admin{background:#162447;color:white;padding:5px 10px;border-radius:12px;}
.badge-arqui{background:#1f4068;color:white;padding:5px 10px;border-radius:12px;}
.badge-gestion{background:#0f3460;color:white;padding:5px 10px;border-radius:12px;}
.fade-enter-active,.fade-leave-active{transition:all .3s;}
.fade-enter-from,.fade-leave-to{opacity:0;transform:translateY(10px);}
</style>