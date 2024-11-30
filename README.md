# Manager Service

#### Responsable de las operaciones principales relacionadas con los snippets. Permite a los usuarios crear, actualizar, eliminar y listar snippets, así como validar su contenido. Es el núcleo de la gestión de snippets en la plataforma.

### Endpoints del Manager Service

* POST `/manager/snippet` 
  Crea un nuevo snippet. 
* PUT `/manager/snippet/update/{id}`
Actualiza un snippet existente.
* DELETE `/manager/snippet/{id}`
Elimina un snippet por su ID.
* POST `/manager/snippet/upload`
Crea un snippet a partir de un archivo cargado.
* GET `/manager/snippet/snippets`
Lista los snippets accesibles por el usuario autenticado.
* GET `/manager/snippet/get`
Recupera los detalles de un snippet por su ID.
* POST  `/manager/snippet/validate`
Valida el contenido de un snippet.

